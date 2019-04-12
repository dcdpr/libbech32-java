package design.contract.bech32;

import java.util.Arrays;
import java.util.Objects;

public class HrpAndDp {
    private String hrp;
    private char[] dp;

    public HrpAndDp(String hrp, char[] dp) {
        this.hrp = hrp;
        this.dp = dp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HrpAndDp hrpAndDp = (HrpAndDp) o;
        return hrp.equals(hrpAndDp.hrp) &&
                Arrays.equals(dp, hrpAndDp.dp);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(hrp);
        result = 31 * result + Arrays.hashCode(dp);
        return result;
    }
}
