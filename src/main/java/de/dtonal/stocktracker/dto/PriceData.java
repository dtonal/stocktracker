package de.dtonal.stocktracker.dto;

import java.math.BigDecimal;

public class PriceData {
    private BigDecimal currentPrice;
    private BigDecimal change;
    private BigDecimal percentChange;
    private BigDecimal highPriceOfDay;
    private BigDecimal lowPriceOfDay;
    private BigDecimal openPriceOfDay;
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

    @Override
    public String toString() {
        return "PriceData{" +
                "currentPrice=" + currentPrice +
                ", change=" + change +
                ", percentChange=" + percentChange +
                ", highPriceOfDay=" + highPriceOfDay +
                ", lowPriceOfDay=" + lowPriceOfDay +
                ", openPriceOfDay=" + openPriceOfDay +
                ", previousClosePrice=" + previousClosePrice +
                '}';
    }
}