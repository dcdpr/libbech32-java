# libbech32-java

This is a Java implementation of "Bech32:" a checksummed base32 data
encoding format. It is primarily used as a new bitcoin address format
specified by [BIP 0173](https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki). 

The libbech32-java package has no external dependencies, except `JUnit` for testing.

## Getting libbech32-java

To use libbech32-java in your project, you can get
the package from Maven Central:

To use the package, you need the following Maven dependency:

```xml
<dependency>
  <groupId>design.contract</groupId>
  <artifactId>libbech32</artifactId>
  <version>1.1.0</version>
</dependency>
```

or download the jar from the Maven repository.

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
        // prints "hello1w0rldjn365x" : "hello" + Bech32.SEPARATOR + encoded data + 6 char checksum

        HrpAndDp hd = Bech32.decode(b);

        assert hd.getHrp().equals("hello");
        assert hd.getDp().length == 5;
        assert hd.getEncoding() == HrpAndDp.Encoding.BECH32M;
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

## Regarding bech32 checksums

The Bech32 data encoding format was first proposed by Pieter Wuille in early 2017 in
[BIP 0173](https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki). Later, in November 2019, Pieter published
some research regarding that an exponent used in the bech32 checksum algorithm (value = 1) may not be
optimal for the error detecting properties of bech32. In February 2021, Pieter published
[BIP 0350](http://www.example.com) reporting that "exhaustive analysis" showed the best possible exponent value is
0x2bc830a3. This improved variant of Bech32 is called "Bech32m".

When decoding a possible bech32 encoded string, libbech32 returns an enum value showing whether bech32m or bech32
was used to encode. This can be seen in the example above.

When encoding data, libbech32 defaults to using the new exponent value of 0x2bc830a3. If the original exponent value
of 1 is desired, then the following functions may be used:

### Usage Example

```java
    /// ... as above ...

    String b = Bech32.encodeUsingOriginalConstant(humanReadablePart, data);

    /// ... as above ...
```


