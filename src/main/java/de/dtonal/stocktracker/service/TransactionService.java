package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.StockTransaction;

public interface TransactionService {
    StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest);
    void deleteStockTransaction(String portfolioId, String transactionId);
} 