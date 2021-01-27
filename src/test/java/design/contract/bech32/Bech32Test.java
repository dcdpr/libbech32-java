package design.contract.bech32;

import org.junit.Test;

import java.util.Arrays;

import static design.contract.bech32.HrpAndDp.Encoding.BECH32;
import static design.contract.bech32.HrpAndDp.Encoding.BECH32M;
import static org.junit.Assert.*;

public class Bech32Test {

    @Test
    public void stripUnknownChars_withNullString_returnsNull() {
        assertNull(Bech32.stripUnknownChars(null));
    }

    @Test
    public void stripUnknownChars_withSimpleString_returnsSameString() {
        String expected = "ace";
        assertEquals(expected, Bech32.stripUnknownChars(expected));
    }

    @Test
    public void stripUnknownChars_withDashes_returnsStrippedString() {
        assertEquals("tx1rqqqqqqqqmhuqk", Bech32.stripUnknownChars("tx1-rqqq-qqqq-qmhu-qk"));
    }

    @Test
    public void stripUnknownChars_withLots_returnsStrippedString() {
        assertEquals("tx1rjk0u5ng4jsfmc", Bech32.stripUnknownChars("tx1!rjk0\\u5ng*4jsf^^mc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringTooShort_withShortString_throws() {
        Bech32.Impl.rejectBStringTooShort("ace");
    }

    @Test()
    public void rejectBStringTooShort_withLongString_wontThrow() {
        Bech32.Impl.rejectBStringTooShort("aceaceace");
    }

    @Test()
    public void rejectBStringTooLong_withShortString_wontThrow() {
        Bech32.Impl.rejectBStringTooLong("aceaceace");
    }

    @Test()
    public void rejectBStringTooLong_withLongerString_wontThrow() {
        char[] buffer = new char[Bech32.Limits.MAX_BECH32_LENGTH - 1];
        Arrays.fill(buffer, 'a');
        Bech32.Impl.rejectBStringTooLong(new String(buffer));
    }

    @Test()
    public void rejectBStringTooLong_withMaxLengthString_wontThrow() {
        char[] buffer = new char[Bech32.Limits.MAX_BECH32_LENGTH];
        Arrays.fill(buffer, 'a');
        Bech32.Impl.rejectBStringTooLong(new String(buffer));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringTooLong_withLongString_throws() {
        char[] buffer = new char[Bech32.Limits.MAX_BECH32_LENGTH + 1];
        Arrays.fill(buffer, 'a');
        Bech32.Impl.rejectBStringTooLong(new String(buffer));
    }

    @Test
    public void rejectBStringMixedCase_withSingleCaseString_wontThrow() {
        Bech32.Impl.rejectBStringMixedCase("abcdefg");
        Bech32.Impl.rejectBStringMixedCase("abc123def");
        Bech32.Impl.rejectBStringMixedCase("12AB34CD");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringMixedCase_withMixedCases_throws() {
        Bech32.Impl.rejectBStringMixedCase("abcDefg");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringMixedCase_withMixedCasesAndNumbers_throws() {
        Bech32.Impl.rejectBStringMixedCase("1abcDefg2");
    }

    @Test
    public void rejectBStringValuesOutOfRange_withInRangeStrings_wontThrow() {
        Bech32.Impl.rejectBStringValuesOutOfRange("abcde");
        Bech32.Impl.rejectBStringValuesOutOfRange("!!abcde}~");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringValuesOutOfRange_withSpaces_throws() {
        Bech32.Impl.rejectBStringValuesOutOfRange("ab cd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringValuesOutOfRange_withNonPrintable_throws() {
        Bech32.Impl.rejectBStringValuesOutOfRange("ab\ncd");
    }

    @Test
    public void rejectBStringWithNoSeparator_withSeparator_wontThrow() {
        Bech32.Impl.rejectBStringWithNoSeparator("ab1cd");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectBStringWithNoSeparator_withNoSeparator_throws() {
        Bech32.Impl.rejectBStringWithNoSeparator("abcd");
    }

    @Test
    public void findSeparatorPosition_withSeparator() {
        int pos = Bech32.Impl.findSeparatorPosition("ab1cd");
        assertEquals(2, pos);

        pos = Bech32.Impl.findSeparatorPosition("abc1def1lalala");
        assertEquals(7, pos);
    }

    @Test
    public void findSeparatorPosition_withoutSeparator() {
        int pos = Bech32.Impl.findSeparatorPosition("");
        assertEquals(-1, pos);

        pos = Bech32.Impl.findSeparatorPosition("lalalala");
        assertEquals(-1, pos);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void splitString_withEmptyString_throws() {
        HrpAndDp hd = Bech32.Impl.splitString("");
    }

    @Test
    public void splitString_withOnlySeparator_returnsEmptyStrings() {
        HrpAndDp hd = Bech32.Impl.splitString("1");
        assertEquals("", hd.getHrp());
        assertEquals(0, hd.getDp().length);
    }

    @Test
    public void splitString_withSeparatorAtStart_returnsOneString() {
        HrpAndDp hd = Bech32.Impl.splitString("1ab");
        assertEquals("", hd.getHrp());
        assertEquals(2, hd.getDp().length);
        assertEquals('a', hd.getDp()[0]);
        assertEquals('b', hd.getDp()[1]);
    }

    @Test
    public void splitString_withSeparatorAtEnd_returnsOneString() {
        HrpAndDp hd = Bech32.Impl.splitString("ab1");
        assertEquals("ab", hd.getHrp());
        assertEquals(0, hd.getDp().length);
    }

    @Test
    public void splitString_withSeparatorInMiddle_returnsTwoStrings() {
        HrpAndDp hd = Bech32.Impl.splitString("ab1cd");
        assertEquals("ab", hd.getHrp());
        assertEquals(2, hd.getDp().length);
        assertEquals('c', hd.getDp()[0]);
        assertEquals('d', hd.getDp()[1]);
    }

    @Test
    public void mapDP_withLowercaseData() {
        HrpAndDp b = Bech32.Impl.splitString("1acd");
        Bech32.Impl.mapDP(b.getDp());
        assertEquals(0x001d, b.getDp()[0]);
        assertEquals(0x0018, b.getDp()[1]);
        assertEquals(0x000d, b.getDp()[2]);
    }

    @Test
    public void mapDP_withUppercaseData() {
        HrpAndDp b = Bech32.Impl.splitString("1ACD");
        Bech32.Impl.mapDP(b.getDp());
        assertEquals(0x001d, b.getDp()[0]);
        assertEquals(0x0018, b.getDp()[1]);
        assertEquals(0x000d, b.getDp()[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void mapDP_withInvalidData_throws() {
        HrpAndDp b = Bech32.Impl.splitString("1abc"); // 'b' is invalid
        Bech32.Impl.mapDP(b.getDp());
    }

    @Test
    public void expandHrp_withUppercaseHrp() {
        String e = Bech32.Impl.expandHrp("ABC");
        assertEquals(0x0002, e.charAt(0));
        assertEquals(0x0002, e.charAt(1));
        assertEquals(0x0002, e.charAt(2));
        assertEquals(0x0000, e.charAt(3));
        assertEquals(0x0001, e.charAt(4));
        assertEquals(0x0002, e.charAt(5));
        assertEquals(0x0003, e.charAt(6));
    }

    @Test
    public void expandHrp_withLowercaseHrp() {
        String e = Bech32.Impl.expandHrp("abc");
        assertEquals(0x0003, e.charAt(0));
        assertEquals(0x0003, e.charAt(1));
        assertEquals(0x0003, e.charAt(2));
        assertEquals(0x0000, e.charAt(3));
        assertEquals(0x0001, e.charAt(4));
        assertEquals(0x0002, e.charAt(5));
        assertEquals(0x0003, e.charAt(6));
    }

    @Test
    public void polymod_short() {
        String e = Bech32.Impl.expandHrp("A");
        long p = Bech32.Impl.polymod(e.toCharArray());
        assertEquals(34817, p);
    }

    @Test
    public void polymod_long() {
        String e = Bech32.Impl.expandHrp("qwerty");
        long p = Bech32.Impl.polymod(e.toCharArray());
        assertEquals(448484437, p);
    }

    @Test
    public void verifyChecksum_withShortHrp_noData_isGood() {
        HrpAndDp b = Bech32.Impl.splitString("a1lqfn3a");
        Bech32.Impl.mapDP(b.getDp());
        assertTrue(Bech32.Impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_c1_withShortHrp_noData_isGood() {
        HrpAndDp b = Bech32.Impl.splitString("a12uel5l");
        Bech32.Impl.mapDP(b.getDp());
        assertTrue(Bech32.Impl.verifyChecksumUsingOriginalConstant(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withLongerHrp_longData_isGood() {
        HrpAndDp b = Bech32.Impl.splitString("abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx");
        Bech32.Impl.mapDP(b.getDp());
        assertTrue(Bech32.Impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_c1_withLongerHrp_longData_isGood() {
        HrpAndDp b = Bech32.Impl.splitString("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        Bech32.Impl.mapDP(b.getDp());
        assertTrue(Bech32.Impl.verifyChecksumUsingOriginalConstant(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withShortHrp_noData_isBad() {
        // this is "bad" because one character from above is changed
        HrpAndDp b = Bech32.Impl.splitString("b12uel5l");
        Bech32.Impl.mapDP(b.getDp());
        assertFalse(Bech32.Impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withLongerHrp_longData_isBad() {
        // this is "bad" because one character from above is changed
        HrpAndDp b = Bech32.Impl.splitString("abcdeg1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        Bech32.Impl.mapDP(b.getDp());
        assertFalse(Bech32.Impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void stripChecksum_inputTooSmall_throws() {
        Bech32.Impl.stripChecksum("abcde");
    }

    @Test
    public void stripChecksum_inputOnlyChecksum_returnsEmptyString() {
        assertEquals(0, Bech32.Impl.stripChecksum("abcdef").length());
    }

    @Test
    public void stripChecksum_inputIsLonger_returnsStringWithoutChecksum() {
        String shortened = Bech32.Impl.stripChecksum("helloabcdef");
        assertEquals("hello", shortened);
    }

    @Test
    public void createChecksum_simple() {
        String hrp = "a";
        char[] data = new char[0];
        char[] checksum = Bech32.Impl.createChecksum(hrp, data).toCharArray();
        assertEquals(0x001f, checksum[0]);
        assertEquals(0x0000, checksum[1]);
        assertEquals(0x0009, checksum[2]);
        assertEquals(0x0013, checksum[3]);
        assertEquals(0x0011, checksum[4]);
        assertEquals(0x001d, checksum[5]);
    }

    @Test
    public void createChecksum_c1_simple() {
        String hrp = "a";
        char[] data = new char[0];
        char[] checksum = Bech32.Impl.createChecksumUsingOriginalConstant(hrp, data).toCharArray();
        assertEquals(0x000a, checksum[0]);
        assertEquals(0x001c, checksum[1]);
        assertEquals(0x0019, checksum[2]);
        assertEquals(0x001f, checksum[3]);
        assertEquals(0x0014, checksum[4]);
        assertEquals(0x001f, checksum[5]);
    }


    @Test(expected = IllegalArgumentException.class)
    public void encode_emptyArgs_throws() {
        String hrp = "";
        char[] data = new char[0];
        Bech32.encode(hrp, data);
    }

    @Test
    public void encode_simple() {
        String hrp = "a";
        char[] data = new char[0];
        String b = Bech32.encode(hrp, data);
        assertEquals("a1lqfn3a", b);
    }

    @Test
    public void encode_c1_simple() {
        String hrp = "a";
        char[] data = new char[0];
        String b = Bech32.encodeUsingOriginalConstant(hrp, data);
        assertEquals("a12uel5l", b);
    }

    @Test(expected = NullPointerException.class)
    public void decode_nullString_throws() {
        HrpAndDp hd = Bech32.decode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decode_emptyString_throws() {
        HrpAndDp hd = Bech32.decode("");
    }

    @Test
    public void decode_stringTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string too short", e.getMessage());
        }
    }

    @Test
    public void decode_stringTooLong_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string too long", e.getMessage());
        }
    }

    @Test
    public void decode_stringMixedCase_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aAaaaaaaaaaaaaaaaa");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string is mixed case", e.getMessage());
        }
    }

    @Test
    public void decode_stringValuesOutOfRange_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a aaaaaaaaaaaaaaaa");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string has value out of range", e.getMessage());
        }
        try {
            String s = "aaaa" + '\u0127' + "aaaa";

            HrpAndDp hd = Bech32.decode(s);
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string has value out of range", e.getMessage());
        }
    }

    @Test
    public void decode_stringNoSeparator_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string is missing separator character", e.getMessage());
        }
    }

    @Test
    public void decode_stringHrpTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("1aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(IllegalArgumentException e) {
            assertEquals("HRP must be at least one character", e.getMessage());
        }
    }

    @Test
    public void decode_stringHrpTooLong_throws() {
        try {
            HrpAndDp hd = Bech32.decode("an84characterlonghumanreadablepartaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1a");
        } catch(IllegalArgumentException e) {
            assertEquals("HRP must be less than 84 characters", e.getMessage());
        }
    }

    @Test
    public void decode_stringDataPartTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a33characterlonghumanreadablepart1a");
        } catch(IllegalArgumentException e) {
            assertEquals("data part must be at least six characters", e.getMessage());
        }
    }

    @Test
    public void decode_badChecksum_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a12uel5m");
        } catch(IllegalArgumentException e) {
            assertEquals("bech32 string has bad checksum", e.getMessage());
        }
    }

    @Test
    public void decode_simple() {
        HrpAndDp hd = Bech32.decode("a1lqfn3a");
        assertEquals("a", hd.getHrp());
        assertEquals(0, hd.getDp().length);
        assertEquals(BECH32M, hd.getEncoding());
    }

    @Test
    public void decode_c1_simple() {
        HrpAndDp hd = Bech32.decode("a12uel5l");
        assertEquals("a", hd.getHrp());
        assertEquals(0, hd.getDp().length);
        assertEquals(BECH32, hd.getEncoding());
    }

    @Test
    public void decode_longer() {
        HrpAndDp hd = Bech32.decode("abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx");
        assertEquals("abcdef", hd.getHrp());
        assertEquals(32, hd.getDp().length);
        assertEquals(0x001f, hd.getDp()[0]); // first 'l' in above data part
        assertEquals(0x0000, hd.getDp()[31]);// last 'q' in above data part
        assertEquals(BECH32M, hd.getEncoding());
    }

    @Test
    public void decode_c1_longer() {
        HrpAndDp hd = Bech32.decode("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        assertEquals("abcdef", hd.getHrp());
        assertEquals(32, hd.getDp().length);
        assertEquals(0x0000, hd.getDp()[0]); // first 'q' in above data part
        assertEquals(0x001f, hd.getDp()[31]);// last 'l' in above data part
        assertEquals(BECH32, hd.getEncoding());
    }

    @Test
    public void decodeThenEncode_givesInitialData_long() {
        String bstr = "abcdef1l7aum6echk45nj3s0wdvt2fg8x9yrzpqzd3ryx";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("abcdef", hd.getHrp());

        String enc = Bech32.encode(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc);
    }

    @Test
    public void decodeThenEncode_c1_givesInitialData_long() {
        String bstr = "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("abcdef", hd.getHrp());

        String enc = Bech32.encodeUsingOriginalConstant(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc);
    }

    @Test
    public void decodeThenEncode_givesInitialData_longer() {
        String bstr = "split1checkupstagehandshakeupstreamerranterredcaperredlc445v";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("split", hd.getHrp());

        String enc = Bech32.encode(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc);
    }
    @Test
    public void decodeThenEncode_c1_givesInitialData_longer() {
        String bstr = "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("split", hd.getHrp());

        String enc = Bech32.encodeUsingOriginalConstant(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc);
    }

}
