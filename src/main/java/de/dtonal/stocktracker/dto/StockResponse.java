package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.Stock;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class StockResponse {
    private String id;
    private String symbol;
    private String name;
    private String exchange;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public StockResponse(Stock stock) {
        this.id = stock.getId();
        this.symbol = stock.getSymbol();
        this.name = stock.getName();
        this.exchange = stock.getExchange();
        this.currency = stock.getCurrency();
        this.createdAt = stock.getCreatedAt();
        this.updatedAt = stock.getUpdatedAt();
    }

    public String getId() {
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