package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.PortfolioUpdateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.StockTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PortfolioService {

    Portfolio createPortfolio(PortfolioCreateRequest createRequest);

    Portfolio updatePortfolio(String portfolioId, PortfolioUpdateRequest updateRequest);

    List<Portfolio> findPortfoliosForCurrentUser();

    Optional<Portfolio> findById(String id);

    StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest);

    void deleteStockTransaction(String portfolioId, String transactionId);

    BigDecimal getStockQuantity(String portfolioId, String stockSymbol);
    
    BigDecimal getTotalPortfolioValue(String portfolioId);

    void deletePortfolio(String portfolioId);
}
