package design.contract.bech32;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

public class Bech32 {

    public static final class Limits {

        // size of the set of character values which are valid for a bech32 string
        public static final int VALID_CHARSET_SIZE = 32;

        // size of the set of "reverse" character values used for decoding
        public static final int REVERSE_CHARSET_SIZE = 128;

        // while there are only 32 valid character values in a bech32 string, other characters
        // can be present but will be stripped out. however, all character values must fall
        // within the following range.
        public static final int MIN_BECH32_CHAR_VALUE = 33;  // ascii '!'
        public static final int MAX_BECH32_CHAR_VALUE = 126; // ascii '~'

        // human-readable part of a bech32 string can only be between 1 and 83 characters long
        public static final int MIN_HRP_LENGTH = 1;
        public static final int MAX_HRP_LENGTH = 83;

        // checksum is always 6 chars long
        public static final int CHECKSUM_LENGTH = 6;

        // entire bech32 string can only be a certain size (after invalid characters are stripped out)
        public static final int MIN_BECH32_LENGTH = 8;  // MIN_HRP_LENGTH + '1' + CHECKSUM_LENGTH
        public static final int MAX_BECH32_LENGTH = 90; // MAX_HRP_LENGTH + '1' + CHECKSUM_LENGTH

        private Limits() {
            throw new IllegalStateException("should not instantiate");
        }
    }

    public static final class Impl {

        // bech32 string must be at least 8 chars long: HRP (min 1 char) + '1' + 6-char checksum
        static void rejectBStringTooShort(final String bstring) {
            if (bstring.length() < Limits.MIN_BECH32_LENGTH)
                throw new IllegalArgumentException("bech32 string too short");
        }

        // bech32 string can be at most 90 characters long
        static void rejectBStringTooLong(final String bstring) {
            if (bstring.length() > Limits.MAX_BECH32_LENGTH)
                throw new IllegalArgumentException("bech32 string too long");
        }

        // bech32 string can not mix upper and lower case
        static void rejectBStringMixedCase(final String bstring) {
            boolean atLeastOneUpper = bstring.chars()
                    .anyMatch(Character::isUpperCase);
            boolean atLeastOneLower = bstring.chars()
                    .anyMatch(Character::isLowerCase);
            if(atLeastOneUpper && atLeastOneLower) {
                throw new IllegalArgumentException("bech32 string is mixed case");
            }
        }

        // bech32 string values must be in range ASCII 33-126
        static void rejectBStringValuesOutOfRange(final String bstring) {
            boolean atLeastOneOutOfRange = bstring.chars()
                    .anyMatch(c -> (c < Limits.MIN_BECH32_CHAR_VALUE || c > Limits.MAX_BECH32_CHAR_VALUE));
            if(atLeastOneOutOfRange) {
                throw new IllegalArgumentException("bech32 string has value out of range");
            }
        }

        // bech32 string must contain the separator character
        static void rejectBStringWithNoSeparator(final String bstring) {
            if(bstring.chars().noneMatch(c -> (c == SEPARATOR))) {
                throw new IllegalArgumentException("bech32 string is missing separator character");
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
            return bstring.lastIndexOf(SEPARATOR);
        }

        // extract the hrp from the string
        static String extractHumanReadablePart(final String bstring) {
            int pos = findSeparatorPosition(bstring);
            return bstring.substring(0, pos);
        }

        // extract the dp from the string
        static char[] extractDataPart(final String bstring) {
            int pos = findSeparatorPosition(bstring);
            return bstring.substring(pos+1).toCharArray();
        }

        // dp needs to be mapped using the charset_rev table
        static void mapDP(char[] dp) {
            for (int i = 0, dpLength = dp.length; i < dpLength; i++) {
                char c = dp[i];
                if (c > Limits.REVERSE_CHARSET_SIZE - 1)
                    throw new IllegalArgumentException("data part contains character value out of range");
                int d = REVERSE_CHARSET[c];
                if (d == -1)
                    throw new IllegalArgumentException("data part contains invalid character");
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
                buf[i] = (char)((int) c >> 5);
                buf[i + hrpLen + 1] = (char)((int) c & 0x1f);
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
        static boolean verifyChecksumBasis(final String hrp, final char[] dp, final int constant) {
            return polymod(cat(expandHrp(hrp).toCharArray(), dp)) == constant;
        }

        // verify the checksum on a Bech32 string
        static boolean verifyChecksum(final String hrp, final char[] dp) {
            return verifyChecksumBasis(hrp, dp, M);
        }

        // verify the checksum on a Bech32 string. This variant of verifyChecksum() uses the
        // constant "1" instead of "M"
        static boolean verifyChecksumUsingOriginalConstant(final String hrp, final char[] dp) {
            return verifyChecksumBasis(hrp, dp, 1);
        }

        // strip off the checksum from a Bech32 string
        static String stripChecksum(final String dp) {
            return dp.substring(0, dp.length() - Limits.CHECKSUM_LENGTH);
        }

        // create a checksum for a given HRP and a DP array
        static String createChecksumBasis(final String hrp, final char[] dp, final int constant) {
            char[] combined = cat(expandHrp(hrp).toCharArray(), dp);
            char[] expanded = Arrays.copyOf(combined, combined.length + Limits.CHECKSUM_LENGTH);

            long mod = polymod(expanded) ^ constant;
            char[] ret = new char[Limits.CHECKSUM_LENGTH];
            for(int i = 0; i < Limits.CHECKSUM_LENGTH; ++i) {
                ret[i] = (char)((mod >> (5 * (5 - i))) & 31);
            }
            return new String(ret);
        }

        // create a checksum for a given HRP and a DP array
        static String createChecksum(final String hrp, final char[] dp) {
            return createChecksumBasis(hrp, dp, M);
        }

        // create a checksum for a given HRP and a DP array. This variant of createChecksum() uses the
        // constant "1" instead of "M"
        static String createChecksumUsingOriginalConstant(final String hrp, final char[] dp) {
            return createChecksumBasis(hrp, dp, 1);
        }

        static void rejectHRPTooShort(final String hrp) {
            if(hrp.length() < Limits.MIN_HRP_LENGTH)
                throw new IllegalArgumentException("HRP must be at least one character");
        }

        static void rejectHRPTooLong(final String hrp) {
            if(hrp.length() > Limits.MAX_HRP_LENGTH)
                throw new IllegalArgumentException("HRP must be less than 84 characters");
        }

        static void rejectDPTooShort(final char [] dp) {
            if(dp.length < Limits.CHECKSUM_LENGTH)
                throw new IllegalArgumentException("data part must be at least six characters");
        }

        // data values must be in range ASCII 0-31 in order to index into the charset
        static void rejectDataValuesOutOfRange(final char[] dp) {
            for(char c : dp) {
                if(c > Limits.VALID_CHARSET_SIZE-1) {
                    throw new IllegalArgumentException("data value is out of range");
                }
            }
        }

        // length of human part plus length of data part plus separator char plus 6 char
        // checksum must be less than 90
        static void rejectBothPartsTooLong(final String hrp, final char[] dp) {
            if(hrp.length() + dp.length + 1 + Limits.CHECKSUM_LENGTH > Limits.MAX_BECH32_LENGTH) {
                throw new IllegalArgumentException("length of hrp + length of dp is too large");
            }
        }

        private Impl() {
            throw new IllegalStateException("should not instantiate");
        }
    }

    // The Bech32 separator character
    public static final char SEPARATOR = '1';

    // constant used in checksum generation. see:
    // https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki
    // https://github.com/bitcoin/bips/blob/master/bip-0350.mediawiki
    static final int M = 0x2bc830a3;

    /* The Bech32 character set for encoding. The index into this string gives the char
     * each value is mapped to, i.e., 0 -> 'q', 10 -> '2', etc. This comes from the table
     * in BIP-0173 */
    static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    /* The Bech32 character set for decoding. This comes from the table in BIP-0173
     *
     * This will help map both upper and lowercase chars into the proper code (or index
     * into the above charset). For instance, 'Q' (ascii 81) and 'q' (ascii 113)
     * are both set to index 0 in this table. Invalid chars are set to -1 */
    static final int[] REVERSE_CHARSET = {
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
    public static String stripUnknownChars(String bstring) {
        if(bstring == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(bstring.length());

        for(char c : bstring.toCharArray()) {
            if(c == SEPARATOR || CHARSET.indexOf(toLowercase(c)) != -1) {
                result.append(c);
            }
        }

        return result.toString();
    }

    // encode a "human-readable part" and a "data part", returning a bech32 string
    private static String encodeBasis(final String hrp, char[] dp, BiFunction<String, char[], String> createChecksumFunc) {
        Objects.requireNonNull(hrp);
        Objects.requireNonNull(dp);

        Impl.rejectHRPTooShort(hrp);
        Impl.rejectHRPTooLong(hrp);
        Impl.rejectBothPartsTooLong(hrp, dp);
        Impl.rejectDataValuesOutOfRange(dp);

        String hrpCopy = hrp.toLowerCase();
        String checksum = createChecksumFunc.apply(hrpCopy, dp);
        char[] combined = Impl.cat(dp, checksum.toCharArray());

        StringBuilder result = new StringBuilder(hrpCopy.length() + 1 + combined.length);
        result.append(hrpCopy);
        result.append(SEPARATOR);

        for (char c : combined) {
            if(c > Limits.VALID_CHARSET_SIZE - 1)
                throw new IllegalArgumentException("data part contains invalid character");
            result.append(CHARSET.charAt(c));
        }
        return result.toString();
    }

    // encode a "human-readable part" and a "data part", returning a bech32 string
    public static String encode(final String hrp, char[] dp) {
        return encodeBasis(hrp, dp, Impl::createChecksum);
    }

    // encode a "human-readable part" and a "data part", returning a bech32 string
    public static String encodeUsingOriginalConstant(final String hrp, char[] dp) {
        return encodeBasis(hrp, dp, Impl::createChecksumUsingOriginalConstant);
    }

    // decode a bech32 string, returning the "human-readable part" and a "data part"
    public static DecodedResult decode(final String bstring) {
        Objects.requireNonNull(bstring);

        Impl.rejectBStringThatIsntWellFormed(bstring);
        String hrp = Impl.extractHumanReadablePart(bstring);
        char[] dp = Impl.extractDataPart(bstring);
        Impl.rejectHRPTooShort(hrp);
        Impl.rejectHRPTooLong(hrp);
        Impl.rejectDPTooShort(dp);
        hrp = hrp.toLowerCase();
        Impl.mapDP(dp);
        if (Impl.verifyChecksum(hrp, dp)) {
            return new DecodedResult(
                    hrp,
                    Impl.stripChecksum(new String(dp)).toCharArray(),
                    DecodedResult.Encoding.BECH32M);
        }
        else if (Impl.verifyChecksumUsingOriginalConstant(hrp, dp)) {
            return new DecodedResult(
                    hrp,
                    Impl.stripChecksum(new String(dp)).toCharArray(),
                    DecodedResult.Encoding.BECH32);
        }
        else {
            return new DecodedResult();
        }

    }

    private Bech32() {
        throw new IllegalStateException("should not instantiate");
    }
}
