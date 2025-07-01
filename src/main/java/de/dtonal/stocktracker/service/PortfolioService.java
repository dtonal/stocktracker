package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PortfolioService {
    Portfolio createPortfolio(String name, String description, User owner);
    Optional<Portfolio> findById(Long portfolioId);
    List<Portfolio> findByUser(User user);
    StockTransaction addTransaction(Long portfolioId, StockTransactionRequest transactionRequest);
    BigDecimal getStockQuantity(Long portfolioId, String stockSymbol);
    BigDecimal getCurrentStockQuantityInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getCurrentStockValueInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getTotalPortfolioValue(Long portfolioId);
    BigDecimal getTotalStockValueInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getTotalStockValueInPortfolio(Long portfolioId);
}
