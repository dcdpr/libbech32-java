package design.contract.example;

import design.contract.bech32.Bech32;
import design.contract.bech32.DecodedResult;

public class Bech32DecodingExample {

    private static void simpleHrp_WithoutData() {
        // this Bech32 string has human-readable part of "hello" and
        // no data part
        String bString = "hello1sn7ru8";

        DecodedResult decodedResult = Bech32.decode(bString);

        assert decodedResult.getHrp().equals("hello");
        assert decodedResult.getDp().length == 0;
        assert decodedResult.getEncoding() == DecodedResult.Encoding.BECH32M;
    }

    private static void simpleHrp_WithData() {
        // this Bech32 string has human-readable part of "hello" and
        // a data part which encodes to "w0rld"
        String bString = "hello1w0rldjn365x";

        DecodedResult decodedResult = Bech32.decode(bString);

        assert decodedResult.getHrp().equals("hello");
        assert decodedResult.getDp().length == 5;
        assert decodedResult.getDp()[0] == 14;
        assert decodedResult.getDp()[1] == 15;
        assert decodedResult.getDp()[2] == 3;
        assert decodedResult.getDp()[3] == 31;
        assert decodedResult.getDp()[4] == 13;
        assert decodedResult.getEncoding() == DecodedResult.Encoding.BECH32M;
    }

    public static void main(String[] args) {

        simpleHrp_WithoutData();
        simpleHrp_WithData();

    }

}
