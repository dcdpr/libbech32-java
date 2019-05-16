package design.contract.example;

import design.contract.bech32.Bech32;

public class Bech32EncodingExample {

    public static void main(String[] args) {

        {
            // simple human readable part, empty data part
            String humanReadablePart = "hello";
            char[] data = new char[0];
            String b = Bech32.encode(humanReadablePart, data);

            System.out.println(b);
            // prints "hello190w0e9" :
            //   "hello" + Bech32.separator ('1') + encoded data (none) + 6 char checksum ("90w0e9")
        }

        {
            // simple human readable part with data part
            String humanReadablePart = "hello";
            char[] data = {14, 15, 3, 31, 13};
            String b = Bech32.encode(humanReadablePart, data);

            System.out.println(b);
            // prints "hello1w0rld80pk3y" :
            //   "hello" + Bech32.separator ('1') + encoded data ("w0rld") + 6 char checksum ("80pk3y")
        }

    }

}
