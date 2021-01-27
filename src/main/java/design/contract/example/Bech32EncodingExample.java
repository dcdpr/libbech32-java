package design.contract.example;

import design.contract.bech32.Bech32;

public class Bech32EncodingExample {

    private static void simpleHrp_WithoutData() {
        // simple human readable part, empty data part
        String humanReadablePart = "hello";
        char[] data = new char[0];
        String b = Bech32.encode(humanReadablePart, data);

        System.out.println(b);
        // prints "hello1sn7ru8" :
        //   "hello" + Bech32.SEPARATOR ('1') + encoded data (none) + 6 char checksum ("sn7ru8")
    }

    private static void simpleHrp_WithData() {
        // simple human readable part with data part
        String humanReadablePart = "hello";
        char[] data = {14, 15, 3, 31, 13};
        String b = Bech32.encode(humanReadablePart, data);

        System.out.println(b);
        // prints "hello1w0rldjn365x" :
        //   "hello" + Bech32.SEPARATOR ('1') + encoded data ("w0rld") + 6 char checksum ("jn365x")
    }

    public static void main(String[] args) {

        simpleHrp_WithoutData();
        simpleHrp_WithData();

    }

}
