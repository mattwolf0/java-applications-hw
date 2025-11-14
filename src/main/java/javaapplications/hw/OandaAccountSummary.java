package javaapplications.hw;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OandaAccountSummary {

    private String id;
    private String alias;
    private String currency;
    private String marginRate;

    @JsonProperty("NAV")
    private String nav;

    private String marginAvailable;
    private Integer openTradeCount;
    private Integer openPositionCount;
    private Integer pendingOrderCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMarginRate() {
        return marginRate;
    }

    public void setMarginRate(String marginRate) {
        this.marginRate = marginRate;
    }

    public String getNav() {
        return nav;
    }

    public void setNav(String nav) {
        this.nav = nav;
    }

    public String getMarginAvailable() {
        return marginAvailable;
    }

    public void setMarginAvailable(String marginAvailable) {
        this.marginAvailable = marginAvailable;
    }

    public Integer getOpenTradeCount() {
        return openTradeCount;
    }

    public void setOpenTradeCount(Integer openTradeCount) {
        this.openTradeCount = openTradeCount;
    }

    public Integer getOpenPositionCount() {
        return openPositionCount;
    }

    public void setOpenPositionCount(Integer openPositionCount) {
        this.openPositionCount = openPositionCount;
    }

    public Integer getPendingOrderCount() {
        return pendingOrderCount;
    }

    public void setPendingOrderCount(Integer pendingOrderCount) {
        this.pendingOrderCount = pendingOrderCount;
    }
}
