package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockTransactionRequest {

    @NotBlank(message = "Stock symbol cannot be blank.")
    private String stockSymbol;

    @NotNull(message = "Transaction date cannot be null.")
    @PastOrPresent(message = "Transaction date cannot be in the future.")
    private LocalDateTime transactionDate;

    @NotNull(message = "Quantity cannot be null.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0.")
    private BigDecimal quantity;

    @NotNull(message = "Price per share cannot be null.")
    @DecimalMin(value = "0.0", message = "Price per share must be a positive value.")
    private BigDecimal pricePerShare;

    @NotNull(message = "Transaction type cannot be null.")
    private TransactionType transactionType;

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
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
} 