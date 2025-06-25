package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Override
    public Portfolio createPortfolio(String name, String description, User owner) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(name);
        portfolio.setDescription(description);
        portfolio.setUser(owner);
        
        return portfolioRepository.save(portfolio);
    }

    @Override
    public Optional<Portfolio> getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId);
    }

    @Override
    public List<Portfolio> getUserPortfolios(User user) {
        return portfolioRepository.findByUser(user);
    }

    @Override
    public StockTransaction addStockTransaction(Long portfolioId, String stockSymbol, 
            BigDecimal quantity, BigDecimal pricePerShare, LocalDate transactionDate, String transactionType) {
        
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));
        
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Aktie mit Symbol " + stockSymbol + " nicht gefunden"));
        
        TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
        
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(portfolio);
        transaction.setStock(stock);
        transaction.setQuantity(quantity);
        transaction.setPricePerShare(pricePerShare);
        transaction.setTransactionDate(transactionDate.atStartOfDay());
        transaction.setTransactionType(type);
 
        return stockTransactionRepository.save(transaction);
    }

    @Override
    public BigDecimal getCurrentStockQuantityInPortfolio(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));
        
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Aktie mit Symbol " + stockSymbol + " nicht gefunden"));
        
        List<StockTransaction> transactions = stockTransactionRepository.findByPortfolioAndStock(portfolio, stock);
        
        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (StockTransaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) {
                totalQuantity = totalQuantity.add(transaction.getQuantity());
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                totalQuantity = totalQuantity.subtract(transaction.getQuantity());
            }
        }
        
        return totalQuantity;
    }

    @Override
    public BigDecimal getCurrentStockValueInPortfolio(Long portfolioId, String stockSymbol) {
        BigDecimal currentQuantity = getCurrentStockQuantityInPortfolio(portfolioId, stockSymbol);
        
        // Hier würde normalerweise der aktuelle Aktienkurs abgerufen werden
        // Für dieses Beispiel verwenden wir den letzten Kaufpreis als Näherung
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));
        
        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Aktie mit Symbol " + stockSymbol + " nicht gefunden"));
        
        List<StockTransaction> transactions = stockTransactionRepository.findByPortfolioAndStock(portfolio, stock);
        
        if (transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Verwende den letzten Kaufpreis als aktuellen Wert
        StockTransaction lastTransaction = transactions.get(transactions.size() - 1);
        return currentQuantity.multiply(lastTransaction.getPricePerShare());
    }

    @Override
    public BigDecimal getTotalPortfolioValue(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));
        
        List<StockTransaction> allTransactions = stockTransactionRepository.findByPortfolio(portfolio);
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (StockTransaction transaction : allTransactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) {
                totalValue = totalValue.add(transaction.getQuantity().multiply(transaction.getPricePerShare()));
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                totalValue = totalValue.subtract(transaction.getQuantity().multiply(transaction.getPricePerShare()));
            }
        }
        
        return totalValue;
    }

    @Override
    public BigDecimal getTotalStockValueInPortfolio(Long portfolioId, String stockSymbol) {
        return getCurrentStockValueInPortfolio(portfolioId, stockSymbol);
    }

    @Override
    public BigDecimal getTotalStockValueInPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));
        
        List<StockTransaction> allTransactions = stockTransactionRepository.findByPortfolio(portfolio);
        
        BigDecimal totalValue = BigDecimal.ZERO;
        for (StockTransaction transaction : allTransactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) {
                totalValue = totalValue.add(transaction.getQuantity().multiply(transaction.getPricePerShare()));
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                totalValue = totalValue.subtract(transaction.getQuantity().multiply(transaction.getPricePerShare()));
            }
        }
        
        return totalValue;
    }
}
