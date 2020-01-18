package design.contract.example;

import design.contract.bech32.Bech32;

public class Bech32EncodingExample {

    private static void simpleHrp_WithoutData() {
        // simple human readable part, empty data part
        String humanReadablePart = "hello";
        char[] data = new char[0];
        String b = Bech32.encode(humanReadablePart, data);

        System.out.println(b);
        // prints "hello16s3sxm" :
        //   "hello" + Bech32.separator ('1') + encoded data (none) + 6 char checksum ("6s3sxm")
    }

    private static void simpleHrp_WithData() {
        // simple human readable part with data part
        String humanReadablePart = "hello";
        char[] data = {14, 15, 3, 31, 13};
        String b = Bech32.encode(humanReadablePart, data);

        System.out.println(b);
        // prints "hello1w0rldcs7fw6" :
        //   "hello" + Bech32.separator ('1') + encoded data ("w0rld") + 6 char checksum ("cs7fw6")
    }

    public static void main(String[] args) {

        simpleHrp_WithoutData();
        simpleHrp_WithData();

    }

}
