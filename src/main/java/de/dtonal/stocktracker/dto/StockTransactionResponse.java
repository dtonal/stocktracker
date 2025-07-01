package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockTransactionResponse {
    private final Long id;
    private final StockResponse stock;
    private final Long portfolioId;
    private final LocalDateTime transactionDate;
    private final BigDecimal quantity;
    private final BigDecimal pricePerShare;
    private final TransactionType transactionType;
    private final LocalDateTime createdAt;

    public StockTransactionResponse(StockTransaction transaction) {
        this.id = transaction.getId();
        this.stock = new StockResponse(transaction.getStock());
        this.portfolioId = transaction.getPortfolio().getId();
        this.transactionDate = transaction.getTransactionDate();
        this.quantity = transaction.getQuantity();
        this.pricePerShare = transaction.getPricePerShare();
        this.transactionType = transaction.getTransactionType();
        this.createdAt = transaction.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public StockResponse getStock() {
        return stock;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
} 