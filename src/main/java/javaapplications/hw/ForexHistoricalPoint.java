package javaapplications.hw;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ForexHistoricalPoint {

    private OffsetDateTime time;
    private BigDecimal close;

    public OffsetDateTime getTime() {
        return time;
    }

    public void setTime(OffsetDateTime time) {
        this.time = time;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }
}
