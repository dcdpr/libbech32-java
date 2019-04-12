package design.contract.bech32;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public interface Bech32 {

    interface limits {

        // size of the set of character values which are valid for a bech32 string
        int VALID_CHARSET_SIZE = 32;

        // size of the set of "reverse" character values used for decoding
        int REVERSE_CHARSET_SIZE = 128;

        // while there are only 32 valid character values in a bech32 string, other characters
        // can be present but will be stripped out. however, all character values must fall
        // within the following range.
        int MIN_BECH32_CHAR_VALUE = 33;  // ascii '!'
        int MAX_BECH32_CHAR_VALUE = 126; // ascii '~'

        // human-readable part of a bech32 string can only be between 1 and 83 characters long
        int MIN_HRP_LENGTH = 1;
        int MAX_HRP_LENGTH = 83;

        // checksum is always 6 chars long
        int CHECKSUM_LENGTH = 6;

        // entire bech32 string can only be a certain size (after invalid characters are stripped out)
        int MIN_BECH32_LENGTH = 8;  // MIN_HRP_LENGTH + '1' + CHECKSUM_LENGTH
        int MAX_BECH32_LENGTH = 90; // MAX_HRP_LENGTH + '1' + CHECKSUM_LENGTH

    }

    interface impl {

        // bech32 string must be at least 8 chars long: HRP (min 1 char) + '1' + 6-char checksum
        static void rejectBStringTooShort(final String bstring) {
            if (bstring.length() < limits.MIN_BECH32_LENGTH)
                throw new RuntimeException("bech32 string too short");
        }

        // bech32 string can be at most 90 characters long
        static void rejectBStringTooLong(final String bstring) {
            if (bstring.length() > limits.MAX_BECH32_LENGTH)
                throw new RuntimeException("bech32 string too long");
        }

        // bech32 string can not mix upper and lower case
        static void rejectBStringMixedCase(final String bstring) {
            boolean atLeastOneUpper = bstring.chars()
                    .anyMatch(Character::isUpperCase);
            boolean atLeastOneLower = bstring.chars()
                    .anyMatch(Character::isLowerCase);
            if(atLeastOneUpper && atLeastOneLower) {
                throw new RuntimeException("bech32 string is mixed case");
            }
        }

        // bech32 string values must be in range ASCII 33-126
        static void rejectBStringValuesOutOfRange(final String bstring) {
            boolean atLeastOneOutOfRange = bstring.chars()
                    .anyMatch(c -> (c < limits.MIN_BECH32_CHAR_VALUE || c > limits.MAX_BECH32_CHAR_VALUE));
            if(atLeastOneOutOfRange) {
                throw new RuntimeException("bech32 string has value out of range");
            }
        }

        // bech32 string must contain the separator character
        static void rejectBStringWithNoSeparator(final String bstring) {
            if(bstring.chars().noneMatch(c -> (c == separator))) {
                throw new RuntimeException("bech32 string is missing separator character");
            }
        }

        // bech32 string must conform to rules laid out in BIP-0173
        static void rejectBStringThatIsntWellFormed(final String bstring) {
            rejectBStringTooShort(bstring);
            rejectBStringTooLong(bstring);
            rejectBStringMixedCase(bstring);
            rejectBStringValuesOutOfRange(bstring);
            rejectBStringWithNoSeparator(bstring);
        }

        // return the position of the separator character
        static int findSeparatorPosition(final String bstring) {
            return bstring.lastIndexOf(separator);
        }

        // split the hrp from the dp at the separator character
        static HrpAndDp splitString(final String bstring) {
            int pos = findSeparatorPosition(bstring);
            String hrp = bstring.substring(0, pos);
            String dpstr = bstring.substring(pos+1);
            return new HrpAndDp(hrp, dpstr.toCharArray());
        }

        // dp needs to be mapped using the charset_rev table
        static void mapDP(char[] dp) {
            for (int i = 0, dpLength = dp.length; i < dpLength; i++) {
                char c = dp[i];
                if (c > limits.REVERSE_CHARSET_SIZE - 1)
                    throw new RuntimeException("data part contains character value out of range");
                int d = charset_rev[c];
                if (d == -1)
                    throw new RuntimeException("data part contains invalid character");
                dp[i] = (char) d;
            }
        }

        // "expand" the HRP -- adapted from example in BIP-0173
        //
        // To expand the chars of the HRP means to create a new collection of
        // the high bits of each character's ASCII value, followed by a zero,
        // and then the low bits of each character. See BIP-0173 for rationale.
        static String expandHrp(final String hrp) {
            int hrpLen = hrp.length();
            char[] buf = new char[hrpLen * 2 + 1];
            for(int i = 0; i < hrpLen; i++) {
                char c = hrp.charAt(i);
                int ci = (int) c;
                buf[i] = (char)(ci >> 5);
                buf[i + hrpLen + 1] = (char)(ci & 0x1f);
            }
            buf[hrpLen] = 0;
            return new String(buf);
        }

        // Find the polynomial with value coefficients mod the generator as 30-bit.
        // Adapted from Pieter Wuille's code in BIP-0173
        static long polymod(final char[] values) {
            long chk = 1;
            for (char value : values) {
                int top = (int)(chk >> 25);
                chk = (
                        (chk & 0x1ffffff) << 5 ^ value ^
                                (-((top     ) & 1) & 0x3b6a57b2) ^
                                (-((top >> 1) & 1) & 0x26508e6d) ^
                                (-((top >> 2) & 1) & 0x1ea119fa) ^
                                (-((top >> 3) & 1) & 0x3d4233dd) ^
                                (-((top >> 4) & 1) & 0x2a1462b3));
            }
            return chk;
        }

        // concatenate two char arrays
        static char[] cat(final char[] x, final char[] y) {
            char[] result = Arrays.copyOf(x, x.length + y.length);
            System.arraycopy(y, 0, result, x.length, y.length);
            return result;
        }

        // verify the checksum on a Bech32 string
        static boolean verifyChecksum(final String hrp, final char[] dp) {
            return polymod(cat(expandHrp(hrp).toCharArray(), dp)) == 1;
        }

        // strip off the checksum from a Bech32 string
        static String stripChecksum(final String dp) {
            return dp.substring(0, dp.length() - limits.CHECKSUM_LENGTH);
        }

        // create a checksum for a given HRP and a DP array
        static String createChecksum(final String hrp, final char[] dp) {
            char[] combined = cat(expandHrp(hrp).toCharArray(), dp);
            char[] expanded = Arrays.copyOf(combined, combined.length + limits.CHECKSUM_LENGTH);

            long mod = polymod(expanded) ^ 1;
            char[] ret = new char[limits.CHECKSUM_LENGTH];
            for(int i = 0; i < limits.CHECKSUM_LENGTH; ++i) {
                ret[i] = (char)((mod >> (5 * (5 - i))) & 31);
            }
            return new String(ret);
        }

        static void rejectHRPTooShort(final String hrp) {
            if(hrp.length() < limits.MIN_HRP_LENGTH)
                throw new RuntimeException("HRP must be at least one character");
        }

        static void rejectHRPTooLong(final String hrp) {
            if(hrp.length() > limits.MAX_HRP_LENGTH)
                throw new RuntimeException("HRP must be less than 84 characters");
        }

        static void rejectDPTooShort(final char [] dp) {
            if(dp.length < limits.CHECKSUM_LENGTH)
                throw new RuntimeException("data part must be at least six characters");
        }

        // data values must be in range ASCII 0-31 in order to index into the charset
        static void rejectDataValuesOutOfRange(final char[] dp) {
            for(char c : dp) {
                if(c > limits.VALID_CHARSET_SIZE-1) {
                    throw new RuntimeException("data value is out of range");
                }
            }
        }

        // length of human part plus length of data part plus separator char plus 6 char
        // checksum must be less than 90
        static void rejectBothPartsTooLong(final String hrp, final char[] dp) {
            if(hrp.length() + dp.length + 1 + limits.CHECKSUM_LENGTH > limits.MAX_BECH32_LENGTH) {
                throw new RuntimeException("length of hrp + length of dp is too large");
            }
        }



    }

    // The Bech32 separator character
    char separator = '1';

    /* The Bech32 character set for encoding. The index into this string gives the char
     * each value is mapped to, i.e., 0 -> 'q', 10 -> '2', etc. This comes from the table
     * in BIP-0173 */
    String charset = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    /* The Bech32 character set for decoding. This comes from the table in BIP-0173
     *
     * This will help map both upper and lowercase chars into the proper code (or index
     * into the above charset). For instance, 'Q' (ascii 81) and 'q' (ascii 113)
     * are both set to index 0 in this table. Invalid chars are set to -1 */
    int[] charset_rev = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            15, -1, 10, 17, 21, 20, 26, 30,  7,  5, -1, -1, -1, -1, -1, -1,
            -1, 29, -1, 24, 13, 25,  9,  8, 23, -1, 18, 22, 31, 27, 19, -1,
            1,  0,  3, 16, 11, 28, 12, 14,  6,  4,  2, -1, -1, -1, -1, -1,
            -1, 29, -1, 24, 13, 25,  9,  8, 23, -1, 18, 22, 31, 27, 19, -1,
            1,  0,  3, 16, 11, 28, 12, 14,  6,  4,  2, -1, -1, -1, -1, -1
    };

    static char toLowercase(char c) {
        if(c >= 65 && c < 91) {
            return (char) (c + 32);
        }
        else {
            return c;
        }
    }

    // clean a bech32 string of any stray characters not in the allowed charset, except for
    // the separator character, which is '1'
    static String stripUnknownChars(String bstring) {
        if(bstring == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(bstring.length());

        for(char c : bstring.toCharArray()) {
            if(c == separator || charset.indexOf(toLowercase(c)) != -1) {
                result.append(c);
            }
        }

        return result.toString();
    }

    // encode a "human-readable part" and a "data part", returning a bech32 string
    static String encode(final String hrp, char[] dp) {
        Objects.requireNonNull(hrp);
        Objects.requireNonNull(dp);

        Bech32.impl.rejectHRPTooShort(hrp);
        Bech32.impl.rejectHRPTooLong(hrp);
        Bech32.impl.rejectBothPartsTooLong(hrp, dp);
        Bech32.impl.rejectDataValuesOutOfRange(dp);

        String hrpCopy = hrp.toLowerCase();
        String checksum = Bech32.impl.createChecksum(hrpCopy, dp);
        char[] combined = Bech32.impl.cat(dp, checksum.toCharArray());

        StringBuilder result = new StringBuilder(hrpCopy.length() + 1 + combined.length);
        result.append(hrpCopy);
        result.append('1');

        for (char c : combined) {
            if(c > Bech32.limits.VALID_CHARSET_SIZE - 1)
                throw new RuntimeException("data part contains invalid character");
            result.append(charset.charAt(c));
        }
        return result.toString();

    }

    // decode a bech32 string, returning the "human-readable part" and a "data part"
    static HrpAndDp decode(final String bstring) {
        Objects.requireNonNull(bstring);

        Bech32.impl.rejectBStringThatIsntWellFormed(bstring);
        HrpAndDp b = Bech32.impl.splitString(bstring);
        Bech32.impl.rejectHRPTooShort(b.getHrp());
        Bech32.impl.rejectHRPTooLong(b.getHrp());
        Bech32.impl.rejectDPTooShort(b.getDp());
        b.setHrp(b.getHrp().toLowerCase());
        Bech32.impl.mapDP(b.getDp());
        if (Bech32.impl.verifyChecksum(b.getHrp(), b.getDp())) {
            b.setDp(Bech32.impl.stripChecksum(new String(b.getDp())).toCharArray());
            return b;
        }
        else {
            throw new RuntimeException("bech32 string has bad checksum");
        }

    }


}
