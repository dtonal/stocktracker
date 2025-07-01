package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import de.dtonal.stocktracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository transactionRepository;

    @Autowired
    public PortfolioServiceImpl(PortfolioRepository portfolioRepository, UserRepository userRepository,
            StockRepository stockRepository, StockTransactionRepository transactionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.stockRepository = stockRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Portfolio createPortfolio(String name, String description, User owner) {
        Portfolio portfolio = new Portfolio(name, description, owner);
        return portfolioRepository.save(portfolio);
    }

    @Override
    @PostAuthorize("returnObject.isEmpty() or returnObject.get().getUser().getEmail() == authentication.name")
    public Optional<Portfolio> findById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId);
    }

    @Override
    public List<Portfolio> findByUser(User user) {
        return portfolioRepository.findByUser(user);
    }

    @Override
    @PreAuthorize("@portfolioRepository.findById(#portfolioId).get().getUser().getEmail() == authentication.name")
    public StockTransaction addTransaction(Long portfolioId, StockTransactionRequest request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        Stock stock = stockRepository.findBySymbol(request.getStockSymbol())
                .orElseGet(() -> {
                    Stock newStock = new Stock(request.getStockSymbol(), "New Stock Name", "N/A", "USD");
                    return stockRepository.save(newStock);
                });

        StockTransaction transaction = new StockTransaction(
                stock,
                portfolio,
                request.getTransactionDate(),
                request.getQuantity(),
                request.getPricePerShare(),
                request.getTransactionType());
        return transactionRepository.save(transaction);
    }

    @Override
    @PreAuthorize("@portfolioRepository.findById(#portfolioId).get().getUser().getEmail() == authentication.name")
    public BigDecimal getStockQuantity(Long portfolioId, String stockSymbol) {
        List<StockTransaction> transactions = transactionRepository.findByPortfolioIdAndStockSymbol(portfolioId,
                stockSymbol);
        BigDecimal quantity = BigDecimal.ZERO;
        for (StockTransaction t : transactions) {
            if (t.getTransactionType() == TransactionType.BUY) {
                quantity = quantity.add(t.getQuantity());
            } else {
                quantity = quantity.subtract(t.getQuantity());
            }
        }
        return quantity;
    }

    @Override
    public BigDecimal getCurrentStockQuantityInPortfolio(Long portfolioId, String stockSymbol) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new IllegalArgumentException("Aktie mit Symbol " + stockSymbol + " nicht gefunden"));

        List<StockTransaction> transactions = transactionRepository.findByPortfolioIdAndStockSymbol(portfolioId,
                stockSymbol);

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

        List<StockTransaction> transactions = transactionRepository.findByPortfolioIdAndStockSymbol(portfolioId,
                stockSymbol);

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

        List<StockTransaction> allTransactions = transactionRepository.findByPortfolioId(portfolioId);

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

        List<StockTransaction> allTransactions = transactionRepository.findByPortfolioId(portfolioId);

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
