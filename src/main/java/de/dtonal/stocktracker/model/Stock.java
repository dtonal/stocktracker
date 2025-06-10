package de.dtonal.stocktracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert die grundlegenden, statischen Informationen eines Wertpapiers (z.B. einer Aktie).
 * Diese Daten müssen nicht für jeden Benutzer separat gespeichert werden, da sie global gültig sind.
 */
@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "symbol", nullable = false, unique = true)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "currency", nullable = false)
    private String currency;

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistoricalPrice> historicalPrices = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Stock() {
        // Default constructor for JPA
    }

    public Stock(String symbol, String name, String exchange, String currency) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<StockTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<StockTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<HistoricalPrice> getHistoricalPrices() {
        return historicalPrices;
    }

    public void setHistoricalPrices(List<HistoricalPrice> historicalPrices) {
        this.historicalPrices = historicalPrices;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", exchange='" + exchange + '\'' +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return id == stock.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
} 