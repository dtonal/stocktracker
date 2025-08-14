package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.PortfolioNotFoundException;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final PortfolioRepository portfolioRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final StockService stockService;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name)")
    public StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found with id: " + portfolioId));

        Stock stock = stockService.getOrCreateStock(transactionRequest.getStockSymbol());

        StockTransaction transaction = createTransactionFromRequest(stock, transactionRequest);

        portfolio.addTransaction(transaction);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Return the managed instance from the saved portfolio, which has the generated ID.
        return savedPortfolio.getTransactions().get(savedPortfolio.getTransactions().size() - 1);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name)")
    public void deleteStockTransaction(String portfolioId, String transactionId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio with ID " + portfolioId + " not found."));

        StockTransaction transactionToRemove = this.stockTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID " + transactionId + " not found."));

        if (!isTransactionInPortfolio(transactionToRemove, portfolio)) {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not belong to portfolio with ID " + portfolioId);
        }

        portfolio.removeTransaction(transactionToRemove);
        portfolioRepository.save(portfolio);
    }
    
    boolean isTransactionInPortfolio(StockTransaction transaction, Portfolio portfolio) {
        return transaction.getPortfolio().getId().equals(portfolio.getId());
    }


    StockTransaction createTransactionFromRequest(Stock stock, StockTransactionRequest transactionRequest) {
        StockTransaction transaction = new StockTransaction();
        transaction.setStock(stock);
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setQuantity(transactionRequest.getQuantity());
        transaction.setPricePerShare(transactionRequest.getPricePerShare());
        transaction.setTransactionDate(transactionRequest.getTransactionDate());
        return transaction;
    }
} 