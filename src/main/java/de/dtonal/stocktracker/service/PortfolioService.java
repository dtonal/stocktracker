package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.User;

public interface PortfolioService {
    Portfolio createPortfolio(String name, String description, User owner);
    Optional<Portfolio> getPortfolioById(Long portfolioId);
    List<Portfolio> getUserPortfolios(User user);
    StockTransaction addStockTransaction(Long portfolioId, String stockSymbol, BigDecimal quantity, BigDecimal pricePerShare, LocalDate transactionDate, String transactionType);
    BigDecimal getCurrentStockQuantityInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getCurrentStockValueInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getTotalPortfolioValue(Long portfolioId);
    BigDecimal getTotalStockValueInPortfolio(Long portfolioId, String stockSymbol);
    BigDecimal getTotalStockValueInPortfolio(Long portfolioId);
}
