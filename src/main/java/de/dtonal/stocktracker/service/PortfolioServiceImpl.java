package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import de.dtonal.stocktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Override
    @Transactional
    public Portfolio createPortfolio(PortfolioCreateRequest createRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));
        Portfolio portfolio = new Portfolio();
        portfolio.setName(createRequest.getName());
        portfolio.setUser(user);
        return portfolioRepository.save(portfolio);
    }

    @Override
    public List<Portfolio> findPortfoliosForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));
        return portfolioRepository.findByUserId(user.getId());
    }

    @Override
    @PostAuthorize("hasRole('ADMIN') or (returnObject.isPresent() and returnObject.get().user.email == principal.username)")
    public Optional<Portfolio> findById(String id) {
        return portfolioRepository.findById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name)")
    public StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        Stock stock = stockRepository.findById(transactionRequest.getStockId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stock not found with id: " + transactionRequest.getStockId()));

        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(portfolio);
        transaction.setStock(stock);
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setQuantity(transactionRequest.getQuantity());
        transaction.setPricePerShare(transactionRequest.getPricePerShare());
        transaction.setTransactionDate(transactionRequest.getTransactionDate());

        return stockTransactionRepository.save(transaction);
    }

    @Override
    @PreAuthorize("@portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name)")
    public BigDecimal getStockQuantity(String portfolioId, String stockSymbol) {
        List<StockTransaction> transactions = stockTransactionRepository.findByPortfolioIdAndStockSymbol(portfolioId,
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
    public BigDecimal getTotalPortfolioValue(String portfolioId) {
        portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio mit ID " + portfolioId + " nicht gefunden"));

        List<StockTransaction> allTransactions = stockTransactionRepository.findByPortfolioId(portfolioId);

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
