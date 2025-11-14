package javaapplications.hw;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ForexTrade {

    private long id;
    private String instrument;
    private BigDecimal units;
    private BigDecimal openPrice;
    private OffsetDateTime openTime;
    private BigDecimal closePrice;
    private OffsetDateTime closeTime;
    private boolean closed;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public void setUnits(BigDecimal units) {
        this.units = units;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public OffsetDateTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(OffsetDateTime openTime) {
        this.openTime = openTime;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public OffsetDateTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(OffsetDateTime closeTime) {
        this.closeTime = closeTime;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getDirection() {
        if (units == null) {
            return "";
        }
        return units.signum() >= 0 ? "Long" : "Short";
    }

    public BigDecimal getAbsoluteUnits() {
        if (units == null) {
            return null;
        }
        return units.abs();
    }

    public BigDecimal getProfit() {
        if (!closed || closePrice == null || units == null || openPrice == null) {
            return null;
        }
        BigDecimal result;
        if (units.signum() >= 0) {
            result = closePrice.subtract(openPrice).multiply(units);
        } else {
            result = openPrice.subtract(closePrice).multiply(units.abs());
        }
        return result;
    }
}
