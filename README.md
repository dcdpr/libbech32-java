# libbech32-java

This is a Java implementation of "Bech32:" a checksummed base32 data
encoding format. It is primarily used as a new bitcoin address format
specified by [BIP 0173](https://github.com/bitcoin/bips/blob/master/bip-0173.mediawiki). 

## Getting and using libbech32-java

To use libbech32-java in your project, you can get
the package from Maven Central:

To use the package, you need the following Maven dependency:

```xml
<dependency>
  <groupId>design.contract</groupId>
  <artifactId>libbech32</artifactId>
  <version>1.0.0</version>
</dependency>
```

or download the jar from the Maven repository.

The libbech32-java package has no external dependencies, except `JUnit` for testing.


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


