# libbech32-java

This is a Java implementation of "Bech32:" a checksummed base32 data
encoding format. It is primarily used as a new bitcoin address format
specified by [BIP 0173](https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki). 

The libbech32-java package has no external dependencies, except `JUnit` for testing.

## Usage Example

```java
import design.contract.bech32.Bech32;

public class EncodingExample {

    public static void main(String[] args) {

            // simple human readable part with data part
            String humanReadablePart = "hello";
            char[] data = {14, 15, 3, 31, 13};
            String b = Bech32.encode(humanReadablePart, data);

            System.out.println(b);
            // prints "hello1w0rldcs7fw6" : "hello" + Bech32.separator + encoded data + 6 char checksum
    }
}
```

## Building libbech32-java

To build libbech32-java, you will need:

* Java (8+)
* Maven

libbech32-java uses a standard maven build process:

```console
mvn install
```

You can also run just the tests without installing:

```console
mvn test
```


