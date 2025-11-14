package javaapplications.hw;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExchangeRatePoint {

    private LocalDate date;
    private BigDecimal rate;

    public ExchangeRatePoint(LocalDate date, BigDecimal rate) {
        this.date = date;
        this.rate = rate;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getRate() {
        return rate;
    }
}
