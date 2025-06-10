package de.dtonal.stocktracker.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Repräsentiert einen einzelnen Kauf oder Verkauf eines Wertpapiers innerhalb eines Portfolios.
 * Wir verfolgen nicht nur die aktuelle Besitzsituation, sondern die einzelnen Transaktionen,
 * um den Einstandspreis und den Gewinn/Verlust genau berechnen zu können.
 */
@Entity
@Table(name = "stock_transactions")
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 6)
    private BigDecimal quantity;

    @Column(name = "price_per_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerShare;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public StockTransaction() {
        // Default constructor for JPA
    }

    public StockTransaction(Stock stock, Portfolio portfolio, LocalDateTime transactionDate,
                          BigDecimal quantity, BigDecimal pricePerShare, TransactionType transactionType) {
        this.stock = stock;
        this.portfolio = portfolio;
        this.transactionDate = transactionDate;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.transactionType = transactionType;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Berechnet den Gesamtwert der Transaktion (quantity * pricePerShare)
     */
    public BigDecimal getTotalValue() {
        return pricePerShare.multiply(quantity);
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
        return "StockTransaction{" +
                "id=" + id +
                ", stock=" + (stock != null ? stock.getSymbol() : "null") +
                ", portfolio=" + (portfolio != null ? portfolio.getId() : "null") +
                ", transactionDate=" + transactionDate +
                ", quantity=" + quantity +
                ", pricePerShare=" + pricePerShare +
                ", transactionType=" + transactionType +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockTransaction that = (StockTransaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
} 