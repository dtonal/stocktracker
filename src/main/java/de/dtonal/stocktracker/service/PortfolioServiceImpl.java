package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.PortfolioUpdateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.PortfolioNotFoundException;
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
    public Optional<Portfolio> findById(String id) {
        Optional<Portfolio> portfolioOpt = portfolioRepository.findById(id);
        if (portfolioOpt.isEmpty()) {
            return Optional.empty();
        }

        Portfolio portfolio = portfolioOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (portfolio.getUser().getEmail().equals(currentUsername) || isAdmin) {
            return portfolioOpt;
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name)")
    public StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        Stock stock = stockRepository.findBySymbol(transactionRequest.getStockSymbol())
                .stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stock not found with symbol: " + transactionRequest.getStockSymbol()));

        StockTransaction transaction = new StockTransaction();
        transaction.setStock(stock);
        transaction.setTransactionType(transactionRequest.getTransactionType());
        transaction.setQuantity(transactionRequest.getQuantity());
        transaction.setPricePerShare(transactionRequest.getPricePerShare());
        transaction.setTransactionDate(transactionRequest.getTransactionDate());
        
        portfolio.addTransaction(transaction);
        
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Return the managed instance from the saved portfolio, which has the generated ID.
        return savedPortfolio.getTransactions().get(savedPortfolio.getTransactions().size() - 1);
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

    @Override
    @Transactional
    public void deletePortfolio(String portfolioId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio with ID " + portfolioId + " not found."));

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.name().equals("ROLE_ADMIN"));

        if (!portfolio.getUser().equals(user) && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this portfolio.");
        }

        portfolioRepository.delete(portfolio);
    }

    @Override
    @Transactional
    public Portfolio updatePortfolio(String portfolioId, PortfolioUpdateRequest updateRequest) {
        Portfolio portfolio = findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio with ID " + portfolioId + " not found."));

        portfolio.setName(updateRequest.getName());
        portfolio.setDescription(updateRequest.getDescription());

        return portfolioRepository.save(portfolio);
    }

    @Override
    @Transactional
    public void deleteStockTransaction(String portfolioId, String transactionId) {
        Portfolio portfolio = this.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio with ID " + portfolioId + " not found."));

        StockTransaction transactionToRemove = this.stockTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction with ID " + transactionId + " not found."));

        if (!transactionToRemove.getPortfolio().getId().equals(portfolio.getId())) {
            // This case should ideally not happen if IDs are unique, but it's a good safeguard.
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not belong to portfolio with ID " + portfolioId);
        }

        // Step 4: Remove the transaction from the portfolio's collection.
        // Because of `orphanRemoval=true` on the @OneToMany relationship,
        // JPA will automatically delete the transaction from the database when the parent is saved/flushed.
        portfolio.removeTransaction(transactionToRemove);
        portfolioRepository.save(portfolio);
    }
}
