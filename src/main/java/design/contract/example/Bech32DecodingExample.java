package design.contract.example;

import design.contract.bech32.Bech32;
import design.contract.bech32.HrpAndDp;

public class Bech32DecodingExample {

    public static void main(String[] args) {

        {
            // this Bech32 string has human-readable part of "hello" and
            // no data part
            String bString = "hello190w0e9";

            HrpAndDp hd = Bech32.decode(bString);

            assert hd.getHrp().equals("hello");
            assert hd.getDp().length == 0;
        }

        {
            // this Bech32 string has human-readable part of "hello" and
            // a data part which encodes to "w0rld"
            String bString = "hello1w0rld80pk3y";

            HrpAndDp hd = Bech32.decode(bString);

            assert hd.getHrp().equals("hello");
            assert hd.getDp().length == 5;
            assert hd.getDp()[0] == 14;
            assert hd.getDp()[1] == 15;
            assert hd.getDp()[2] == 3;
            assert hd.getDp()[3] == 31;
            assert hd.getDp()[4] == 13;

        }

    }

}
