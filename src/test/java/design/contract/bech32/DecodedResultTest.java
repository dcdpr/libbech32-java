package design.contract.bech32;

import org.junit.Test;

import static org.junit.Assert.*;

public class DecodedResultTest {

    @Test
    public void getHrp() {
        String expected = "hello";
        DecodedResult decodedResult = new DecodedResult(expected, null);
        assertEquals(expected, decodedResult.getHrp());
    }

    @Test
    public void setHrp() {
        DecodedResult decodedResult = new DecodedResult(null, null);
        String expected = "hello";
        decodedResult.setHrp(expected);
        assertEquals(expected, decodedResult.getHrp());
    }

    @Test
    public void getDp() {
        char[] expected = {0,1,2};
        DecodedResult decodedResult = new DecodedResult(null, expected);
        assertEquals(expected, decodedResult.getDp());
    }

    @Test
    public void setDp() {
        DecodedResult decodedResult = new DecodedResult(null, null);
        char[] expected = {0,1,2};
        decodedResult.setDp(expected);
        assertEquals(expected, decodedResult.getDp());
    }

    @Test
    public void getEncoding() {
        DecodedResult.Encoding expected = DecodedResult.Encoding.UNKNOWN;
        DecodedResult decodedResult = new DecodedResult("hello", null);
        assertEquals(expected, decodedResult.getEncoding());
    }

    @Test
    public void setEncoding() {
        DecodedResult.Encoding expected = DecodedResult.Encoding.BECH32M;
        DecodedResult decodedResult = new DecodedResult("hello", null);
        decodedResult.setEncoding(DecodedResult.Encoding.BECH32M);
        assertEquals(expected, decodedResult.getEncoding());
    }

    @Test
    public void testEquals_Reflexive() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        assertEquals(a, a);
    }

    @Test
    public void testEquals_Symmetric() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        DecodedResult b = new DecodedResult("a", dp);

        assertTrue(a.equals(b) && b.equals(a));
    }

    @Test
    public void testEquals_Transitive() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        DecodedResult b = new DecodedResult("a", dp);
        DecodedResult c = new DecodedResult("a", dp);

        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    public void testNotEquals() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        DecodedResult b = new DecodedResult("b", dp);

        assertNotEquals(b, a);
    }

    @Test
    public void testHashCode() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        DecodedResult b = new DecodedResult("a", dp);
        assertEquals(b.hashCode(), a.hashCode());
    }

    @Test
    public void testHashCodeNotEquals() {
        char[] dp = {0,1,2};
        DecodedResult a = new DecodedResult("a", dp);
        DecodedResult b = new DecodedResult("b", dp);

        assertNotEquals(b.hashCode(), a.hashCode());
    }

}
