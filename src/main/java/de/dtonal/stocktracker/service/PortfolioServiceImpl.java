package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioServiceImpl implements PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final PortfolioCalculationService portfolioCalculationService;

    @Override
    @Transactional
    public Portfolio createPortfolio(PortfolioCreateRequest createRequest) {
        User user = getCurrentUser();
        Portfolio portfolio = new Portfolio();
        portfolio.setName(createRequest.getName());
        portfolio.setDescription(createRequest.getDescription());
        portfolio.setUser(user);
        return portfolioRepository.save(portfolio);
    }

    @Override
    public List<Portfolio> findPortfoliosForCurrentUser() {
        User user = getCurrentUser();
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

        if (isUserOwnerOrAdmin(portfolio, authentication)) {
            return portfolioOpt;
        } else {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public StockTransaction addStockTransaction(String portfolioId, StockTransactionRequest transactionRequest) {
        return transactionService.addStockTransaction(portfolioId, transactionRequest);
    }

    @Override
    public BigDecimal getStockQuantity(String portfolioId, String stockSymbol) {
        return portfolioCalculationService.getStockQuantity(portfolioId, stockSymbol);
    }

    @Override
    public BigDecimal getTotalPortfolioValue(String portfolioId) {
        return portfolioCalculationService.getTotalPortfolioValue(portfolioId);
    }

    @Override
    @Transactional
    public void deletePortfolio(String portfolioId) {
        User user = getCurrentUser();

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio with ID " + portfolioId + " not found."));

        authorizePortfolioAccess(portfolio, user);

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
        transactionService.deleteStockTransaction(portfolioId, transactionId);
    }
    
    // --- Helper methods for pure logic ---

    void authorizePortfolioAccess(Portfolio portfolio, User user) {
        boolean isAdmin = user.getRoles().contains(Role.ADMIN);

        if (!portfolio.getUser().equals(user) && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to delete this portfolio.");
        }
    }

    boolean isUserOwnerOrAdmin(Portfolio portfolio, Authentication authentication) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        
        return portfolio.getUser().getEmail().equals(currentUsername) || isAdmin;
    }

    // --- Helper methods with dependencies ---
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for token"));
    }
}
