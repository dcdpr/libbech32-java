package design.contract.example;

import design.contract.bech32.Bech32;
import design.contract.bech32.HrpAndDp;

public class Bech32DecodingExample {

    private static void simpleHrp_WithoutData() {
        // this Bech32 string has human-readable part of "hello" and
        // no data part
        String bString = "hello1sn7ru8";

        HrpAndDp hd = Bech32.decode(bString);

        assert hd.getHrp().equals("hello");
        assert hd.getDp().length == 0;
        assert hd.getEncoding() == HrpAndDp.Encoding.BECH32M;
    }

    private static void simpleHrp_WithData() {
        // this Bech32 string has human-readable part of "hello" and
        // a data part which encodes to "w0rld"
        String bString = "hello1w0rldjn365x";

        HrpAndDp hd = Bech32.decode(bString);

        assert hd.getHrp().equals("hello");
        assert hd.getDp().length == 5;
        assert hd.getDp()[0] == 14;
        assert hd.getDp()[1] == 15;
        assert hd.getDp()[2] == 3;
        assert hd.getDp()[3] == 31;
        assert hd.getDp()[4] == 13;
        assert hd.getEncoding() == HrpAndDp.Encoding.BECH32M;
    }

    public static void main(String[] args) {

        simpleHrp_WithoutData();
        simpleHrp_WithData();

    }

}
