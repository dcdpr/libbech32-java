package design.contract.bech32;

import java.util.Arrays;
import java.util.Objects;

public class DecodedResult {
    private String hrp;
    private char[] dp;
    private Encoding encoding;

    public DecodedResult() {
        this.encoding = Encoding.INVALID;
    }

    public DecodedResult(String hrp, char[] dp) {
        this.hrp = hrp;
        this.dp = dp;
        this.encoding = Encoding.INVALID;
    }

    public DecodedResult(String hrp, char[] dp, Encoding encoding) {
        this.hrp = hrp;
        this.dp = dp;
        this.encoding = encoding;
    }

    public String getHrp() {
        return hrp;
    }

    public void setHrp(String hrp) {
        this.hrp = hrp;
    }

    public char[] getDp() {
        return dp;
    }

    public void setDp(char[] dp) {
        this.dp = dp;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DecodedResult decodedResult = (DecodedResult) o;
        return hrp.equals(decodedResult.hrp) &&
                Arrays.equals(dp, decodedResult.dp) &&
                encoding == decodedResult.encoding;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(hrp, encoding);
        result = 31 * result + Arrays.hashCode(dp);
        return result;
    }

    public enum Encoding {
        INVALID, // no or invalid encoding was detected
        BECH32,  // encoding used original checksum constant (1)
        BECH32M; // encoding used default checksum constant (M = 0x2bc830a3)
    }
}
