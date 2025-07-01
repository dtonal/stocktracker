package de.dtonal.stocktracker.repository;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    
    /**
     * Findet alle Transaktionen eines Portfolios
     */
    List<StockTransaction> findByPortfolio(Portfolio portfolio);
    
    /**
     * Findet alle Transaktionen eines Portfolios anhand der Portfolio-ID
     */
    List<StockTransaction> findByPortfolioId(Long portfolioId);
    
    /**
     * Findet alle Transaktionen für eine bestimmte Aktie
     */
    List<StockTransaction> findByStock(Stock stock);
    
    /**
     * Findet alle Transaktionen für eine bestimmte Aktie anhand der Stock-ID
     */
    List<StockTransaction> findByStockId(Long stockId);
    
    /**
     * Findet alle Transaktionen eines bestimmten Typs (BUY/SELL) in einem Portfolio
     */
    List<StockTransaction> findByPortfolioAndTransactionType(Portfolio portfolio, TransactionType transactionType);
    
    /**
     * Findet alle Transaktionen in einem Zeitraum für ein Portfolio
     */
    List<StockTransaction> findByPortfolioAndTransactionDateBetween(
            Portfolio portfolio, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Findet alle Transaktionen für eine bestimmte Aktie in einem Portfolio
     */
    List<StockTransaction> findByPortfolioAndStock(Portfolio portfolio, Stock stock);

    /**
     * Findet alle Transaktionen eines Portfolios anhand der Aktie und der Portfolio-ID
     */
    List<StockTransaction> findByPortfolioIdAndStockSymbol(Long portfolioId, String stockSymbol);
} 