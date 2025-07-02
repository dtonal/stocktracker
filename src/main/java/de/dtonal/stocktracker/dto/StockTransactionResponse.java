package de.dtonal.stocktracker.dto;

import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransactionResponse {
    private String id;
    private String stockId;
    private String stockSymbol;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal pricePerShare;
    private LocalDateTime transactionDate;

    public StockTransactionResponse(StockTransaction transaction) {
        this.id = transaction.getId();
        this.stockId = transaction.getStock().getId();
        this.stockSymbol = transaction.getStock().getSymbol();
        this.transactionType = transaction.getTransactionType();
        this.quantity = transaction.getQuantity();
        this.pricePerShare = transaction.getPricePerShare();
        this.transactionDate = transaction.getTransactionDate();
    }
} 