package design.contract.bech32;

import org.junit.Test;

import java.util.Arrays;

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

    @Test(expected = RuntimeException.class)
    public void rejectBStringTooShort_withShortString_throws() {
        Bech32.impl.rejectBStringTooShort("ace");
    }

    @Test()
    public void rejectBStringTooShort_withLongString_wontThrow() {
        Bech32.impl.rejectBStringTooShort("aceaceace");
    }

    @Test()
    public void rejectBStringTooLong_withShortString_wontThrow() {
        Bech32.impl.rejectBStringTooLong("aceaceace");
    }

    @Test()
    public void rejectBStringTooLong_withLongerString_wontThrow() {
        char[] buffer = new char[Bech32.limits.MAX_BECH32_LENGTH - 1];
        Arrays.fill(buffer, 'a');
        Bech32.impl.rejectBStringTooLong(new String(buffer));
    }

    @Test()
    public void rejectBStringTooLong_withMaxLengthString_wontThrow() {
        char[] buffer = new char[Bech32.limits.MAX_BECH32_LENGTH];
        Arrays.fill(buffer, 'a');
        Bech32.impl.rejectBStringTooLong(new String(buffer));
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringTooLong_withLongString_throws() {
        char[] buffer = new char[Bech32.limits.MAX_BECH32_LENGTH + 1];
        Arrays.fill(buffer, 'a');
        Bech32.impl.rejectBStringTooLong(new String(buffer));
    }

    @Test
    public void rejectBStringMixedCase_withSingleCaseString_wontThrow() {
        Bech32.impl.rejectBStringMixedCase("abcdefg");
        Bech32.impl.rejectBStringMixedCase("abc123def");
        Bech32.impl.rejectBStringMixedCase("12AB34CD");
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringMixedCase_withMixedCases_throws() {
        Bech32.impl.rejectBStringMixedCase("abcDefg");
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringMixedCase_withMixedCasesAndNumbers_throws() {
        Bech32.impl.rejectBStringMixedCase("1abcDefg2");
    }

    @Test
    public void rejectBStringValuesOutOfRange_withInRangeStrings_wontThrow() {
        Bech32.impl.rejectBStringValuesOutOfRange("abcde");
        Bech32.impl.rejectBStringValuesOutOfRange("!!abcde}~");
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringValuesOutOfRange_withSpaces_throws() {
        Bech32.impl.rejectBStringValuesOutOfRange("ab cd");
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringValuesOutOfRange_withNonPrintable_throws() {
        Bech32.impl.rejectBStringValuesOutOfRange("ab\ncd");
    }

    @Test
    public void rejectBStringWithNoSeparator_withSeparator_wontThrow() {
        Bech32.impl.rejectBStringWithNoSeparator("ab1cd");
    }

    @Test(expected = RuntimeException.class)
    public void rejectBStringWithNoSeparator_withNoSeparator_throws() {
        Bech32.impl.rejectBStringWithNoSeparator("abcd");
    }

    @Test
    public void findSeparatorPosition_withSeparator() {
        int pos = Bech32.impl.findSeparatorPosition("ab1cd");
        assertEquals(2, pos);

        pos = Bech32.impl.findSeparatorPosition("abc1def1lalala");
        assertEquals(7, pos);
    }

    @Test
    public void findSeparatorPosition_withoutSeparator() {
        int pos = Bech32.impl.findSeparatorPosition("");
        assertEquals(-1, pos);

        pos = Bech32.impl.findSeparatorPosition("lalalala");
        assertEquals(-1, pos);
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void splitString_withEmptyString_throws() {
        HrpAndDp hd = Bech32.impl.splitString("");
    }

    @Test
    public void splitString_withOnlySeparator_returnsEmptyStrings() {
        HrpAndDp hd = Bech32.impl.splitString("1");
        assertEquals("", hd.getHrp());
        assertEquals(0, hd.getDp().length);
    }

    @Test
    public void splitString_withSeparatorAtStart_returnsOneString() {
        HrpAndDp hd = Bech32.impl.splitString("1ab");
        assertEquals("", hd.getHrp());
        assertEquals(2, hd.getDp().length);
        assertEquals('a', hd.getDp()[0]);
        assertEquals('b', hd.getDp()[1]);
    }

    @Test
    public void splitString_withSeparatorAtEnd_returnsOneString() {
        HrpAndDp hd = Bech32.impl.splitString("ab1");
        assertEquals("ab", hd.getHrp());
        assertEquals(0, hd.getDp().length);
    }

    @Test
    public void splitString_withSeparatorInMiddle_returnsTwoStrings() {
        HrpAndDp hd = Bech32.impl.splitString("ab1cd");
        assertEquals("ab", hd.getHrp());
        assertEquals(2, hd.getDp().length);
        assertEquals('c', hd.getDp()[0]);
        assertEquals('d', hd.getDp()[1]);
    }

    @Test
    public void mapDP_withLowercaseData() {
        HrpAndDp b = Bech32.impl.splitString("1acd");
        Bech32.impl.mapDP(b.getDp());
        assertEquals(b.getDp()[0], 0x001d);
        assertEquals(b.getDp()[1], 0x0018);
        assertEquals(b.getDp()[2], 0x000d);
    }

    @Test
    public void mapDP_withUppercaseData() {
        HrpAndDp b = Bech32.impl.splitString("1ACD");
        Bech32.impl.mapDP(b.getDp());
        assertEquals(b.getDp()[0], 0x001d);
        assertEquals(b.getDp()[1], 0x0018);
        assertEquals(b.getDp()[2], 0x000d);
    }

    @Test(expected = RuntimeException.class)
    public void mapDP_withInvalidData_throws() {
        HrpAndDp b = Bech32.impl.splitString("1abc"); // 'b' is invalid
        Bech32.impl.mapDP(b.getDp());
    }

    @Test
    public void expandHrp_withUppercaseHrp() {
        String e = Bech32.impl.expandHrp("ABC");
        assertEquals(e.charAt(0), 0x0002);
        assertEquals(e.charAt(1), 0x0002);
        assertEquals(e.charAt(2), 0x0002);
        assertEquals(e.charAt(3), 0x0000);
        assertEquals(e.charAt(4), 0x0001);
        assertEquals(e.charAt(5), 0x0002);
        assertEquals(e.charAt(6), 0x0003);
    }

    @Test
    public void expandHrp_withLowercaseHrp() {
        String e = Bech32.impl.expandHrp("abc");
        assertEquals(e.charAt(0), 0x0003);
        assertEquals(e.charAt(1), 0x0003);
        assertEquals(e.charAt(2), 0x0003);
        assertEquals(e.charAt(3), 0x0000);
        assertEquals(e.charAt(4), 0x0001);
        assertEquals(e.charAt(5), 0x0002);
        assertEquals(e.charAt(6), 0x0003);
    }

    @Test
    public void polymod_short() {
        String e = Bech32.impl.expandHrp("A");
        long p = Bech32.impl.polymod(e.toCharArray());
        assertEquals(p, 34817);
    }

    @Test
    public void polymod_long() {
        String e = Bech32.impl.expandHrp("qwerty");
        long p = Bech32.impl.polymod(e.toCharArray());
        assertEquals(p, 448484437);
    }

    @Test
    public void verifyChecksum_withShortHrp_noData_isGood() {
        HrpAndDp b = Bech32.impl.splitString("a12uel5l");
        Bech32.impl.mapDP(b.getDp());
        assertTrue(Bech32.impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withLongerHrp_longData_isGood() {
        HrpAndDp b = Bech32.impl.splitString("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        Bech32.impl.mapDP(b.getDp());
        assertTrue(Bech32.impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withShortHrp_noData_isBad() {
        // this is "bad" because one character from above is changed
        HrpAndDp b = Bech32.impl.splitString("b12uel5l");
        Bech32.impl.mapDP(b.getDp());
        assertFalse(Bech32.impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test
    public void verifyChecksum_withLongerHrp_longData_isBad() {
        // this is "bad" because one character from above is changed
        HrpAndDp b = Bech32.impl.splitString("abcdeg1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        Bech32.impl.mapDP(b.getDp());
        assertFalse(Bech32.impl.verifyChecksum(b.getHrp(), b.getDp()));
    }

    @Test(expected = RuntimeException.class)
    public void stripChecksum_inputTooSmall_throws() {
        Bech32.impl.stripChecksum("abcde");
    }

    @Test
    public void stripChecksum_inputOnlyChecksum_returnsEmptyString() {
        assertEquals(0, Bech32.impl.stripChecksum("abcdef").length());
    }

    @Test
    public void stripChecksum_inputIsLonger_returnsStringWithoutChecksum() {
        String shortened = Bech32.impl.stripChecksum("helloabcdef");
        assertEquals("hello", shortened);
    }

    @Test
    public void createChecksum_simple() {
        String hrp = "A";
        char[] data = new char[0];
        char[] checksum = Bech32.impl.createChecksum(hrp, data).toCharArray();
        assertEquals(0x0008, checksum[0]);
        assertEquals(0x001e, checksum[1]);
        assertEquals(0x0010, checksum[2]);
        assertEquals(0x0008, checksum[3]);
        assertEquals(0x000d, checksum[4]);
        assertEquals(0x0007, checksum[5]);
    }


    @Test(expected = RuntimeException.class)
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
        assertEquals(b, "a12uel5l");
    }

    @Test(expected = RuntimeException.class)
    public void decode_nullString_throws() {
        HrpAndDp hd = Bech32.decode(null);
    }

    @Test(expected = RuntimeException.class)
    public void decode_emptyString_throws() {
        HrpAndDp hd = Bech32.decode("");
    }

    @Test
    public void decode_stringTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a");
        } catch(RuntimeException e) {
            assertEquals("bech32 string too short", e.getMessage());
        }
    }

    @Test
    public void decode_stringTooLong_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(RuntimeException e) {
            assertEquals("bech32 string too long", e.getMessage());
        }
    }

    @Test
    public void decode_stringMixedCase_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aAaaaaaaaaaaaaaaaa");
        } catch(RuntimeException e) {
            assertEquals("bech32 string is mixed case", e.getMessage());
        }
    }

    @Test
    public void decode_stringValuesOutOfRange_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a aaaaaaaaaaaaaaaa");
        } catch(RuntimeException e) {
            assertEquals("bech32 string has value out of range", e.getMessage());
        }
        try {
            String s = "aaaa" + '\u0127' + "aaaa";

            HrpAndDp hd = Bech32.decode(s);
        } catch(RuntimeException e) {
            assertEquals("bech32 string has value out of range", e.getMessage());
        }
    }

    @Test
    public void decode_stringNoSeparator_throws() {
        try {
            HrpAndDp hd = Bech32.decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(RuntimeException e) {
            assertEquals("bech32 string is missing separator character", e.getMessage());
        }
    }

    @Test
    public void decode_stringHrpTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("1aaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        } catch(RuntimeException e) {
            assertEquals("HRP must be at least one character", e.getMessage());
        }
    }

    @Test
    public void decode_stringHrpTooLong_throws() {
        try {
            HrpAndDp hd = Bech32.decode("an84characterlonghumanreadablepartaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1a");
        } catch(RuntimeException e) {
            assertEquals("HRP must be less than 84 characters", e.getMessage());
        }
    }

    @Test
    public void decode_stringDataPartTooShort_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a33characterlonghumanreadablepart1a");
        } catch(RuntimeException e) {
            assertEquals("data part must be at least six characters", e.getMessage());
        }
    }

    @Test
    public void decode_badChecksum_throws() {
        try {
            HrpAndDp hd = Bech32.decode("a12uel5m");
        } catch(RuntimeException e) {
            assertEquals("bech32 string has bad checksum", e.getMessage());
        }
    }

    @Test
    public void decode_simple() {
        HrpAndDp hd = Bech32.decode("a12uel5l");
        assertEquals("a", hd.getHrp());
        assertEquals(0, hd.getDp().length);
    }

    @Test
    public void decode_longer() {
        HrpAndDp hd = Bech32.decode("abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw");
        assertEquals("abcdef", hd.getHrp());
        assertEquals(32, hd.getDp().length);
        assertEquals(0x0000, hd.getDp()[0]); // 'q' in above data part
        assertEquals(0x001f, hd.getDp()[31]);// 'l' in above data part
    }

    @Test
    public void decodeThenEncode_givesInitialData_1() {
        String bstr = "abcdef1qpzry9x8gf2tvdw0s3jn54khce6mua7lmqqqxw";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("abcdef", hd.getHrp());

        String enc = Bech32.encode(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc, bstr);

    }

    @Test
    public void decodeThenEncode_givesInitialData_2() {
        String bstr = "split1checkupstagehandshakeupstreamerranterredcaperred2y9e3w";

        HrpAndDp hd = Bech32.decode(bstr);
        assertEquals("split", hd.getHrp());

        String enc = Bech32.encode(hd.getHrp(), hd.getDp());
        assertEquals(bstr, enc, bstr);

    }

}
