package design.contract.bech32;

import org.junit.Test;

import static org.junit.Assert.*;

public class HrpAndDpTest {

    @Test
    public void getHrp() {
        String expected = "hello";
        HrpAndDp hrpAndDp = new HrpAndDp(expected, null);
        assertEquals(expected, hrpAndDp.getHrp());
    }

    @Test
    public void setHrp() {
        HrpAndDp hrpAndDp = new HrpAndDp(null, null);
        String expected = "hello";
        hrpAndDp.setHrp(expected);
        assertEquals(expected, hrpAndDp.getHrp());
    }

    @Test
    public void getDp() {
        char[] expected = {0,1,2};
        HrpAndDp hrpAndDp = new HrpAndDp(null, expected);
        assertEquals(expected, hrpAndDp.getDp());
    }

    @Test
    public void setDp() {
        HrpAndDp hrpAndDp = new HrpAndDp(null, null);
        char[] expected = {0,1,2};
        hrpAndDp.setDp(expected);
        assertEquals(expected, hrpAndDp.getDp());
    }

    @Test
    public void getEncoding() {
        HrpAndDp.Encoding expected = HrpAndDp.Encoding.UNKNOWN;
        HrpAndDp hrpAndDp = new HrpAndDp("hello", null);
        assertEquals(expected, hrpAndDp.getEncoding());
    }

    @Test
    public void setEncoding() {
        HrpAndDp.Encoding expected = HrpAndDp.Encoding.BECH32M;
        HrpAndDp hrpAndDp = new HrpAndDp("hello", null);
        hrpAndDp.setEncoding(HrpAndDp.Encoding.BECH32M);
        assertEquals(expected, hrpAndDp.getEncoding());
    }

    @Test
    public void testEquals_Reflexive() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        assertEquals(a, a);
    }

    @Test
    public void testEquals_Symmetric() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        HrpAndDp b = new HrpAndDp("a", dp);

        assertTrue(a.equals(b) && b.equals(a));
    }

    @Test
    public void testEquals_Transitive() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        HrpAndDp b = new HrpAndDp("a", dp);
        HrpAndDp c = new HrpAndDp("a", dp);

        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
    }

    @Test
    public void testNotEquals() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        HrpAndDp b = new HrpAndDp("b", dp);

        assertNotEquals(b, a);
    }

    @Test
    public void testHashCode() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        HrpAndDp b = new HrpAndDp("a", dp);
        assertEquals(b.hashCode(), a.hashCode());
    }

    @Test
    public void testHashCodeNotEquals() {
        char[] dp = {0,1,2};
        HrpAndDp a = new HrpAndDp("a", dp);
        HrpAndDp b = new HrpAndDp("b", dp);

        assertNotEquals(b.hashCode(), a.hashCode());
    }

}
