package de.dtonal.stocktracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class FinnhubQuote {
    @JsonProperty("c")
    private BigDecimal currentPrice;

    @JsonProperty("d")
    private BigDecimal change;

    @JsonProperty("dp")
    private BigDecimal percentChange;

    @JsonProperty("h")
    private BigDecimal highPriceOfDay;

    @JsonProperty("l")
    private BigDecimal lowPriceOfDay;

    @JsonProperty("o")
    private BigDecimal openPriceOfDay;

    @JsonProperty("pc")
    private BigDecimal previousClosePrice;

    // Getters and Setters
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getPercentChange() {
        return percentChange;
    }

    public void setPercentChange(BigDecimal percentChange) {
        this.percentChange = percentChange;
    }

    public BigDecimal getHighPriceOfDay() {
        return highPriceOfDay;
    }

    public void setHighPriceOfDay(BigDecimal highPriceOfDay) {
        this.highPriceOfDay = highPriceOfDay;
    }

    public BigDecimal getLowPriceOfDay() {
        return lowPriceOfDay;
    }

    public void setLowPriceOfDay(BigDecimal lowPriceOfDay) {
        this.lowPriceOfDay = lowPriceOfDay;
    }

    public BigDecimal getOpenPriceOfDay() {
        return openPriceOfDay;
    }

    public void setOpenPriceOfDay(BigDecimal openPriceOfDay) {
        this.openPriceOfDay = openPriceOfDay;
    }

    public BigDecimal getPreviousClosePrice() {
        return previousClosePrice;
    }

    public void setPreviousClosePrice(BigDecimal previousClosePrice) {
        this.previousClosePrice = previousClosePrice;
    }
} 