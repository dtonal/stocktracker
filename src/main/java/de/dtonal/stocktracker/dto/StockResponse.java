package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.Stock;

public class StockResponse {
    private final Long id;
    private final String symbol;
    private final String name;
    private final String exchange;
    private final String currency;

    public StockResponse(Stock stock) {
        this.id = stock.getId();
        this.symbol = stock.getSymbol();
        this.name = stock.getName();
        this.exchange = stock.getExchange();
        this.currency = stock.getCurrency();
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getExchange() {
        return exchange;
    }

    public String getCurrency() {
        return currency;
    }
} 