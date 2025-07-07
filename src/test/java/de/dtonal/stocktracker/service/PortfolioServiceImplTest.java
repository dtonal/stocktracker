package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
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

@SpringBootTest
class PortfolioServiceImplTest {

    @Autowired
    private PortfolioService portfolioService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
    @MockBean
    private UserRepository userRepository;

    private User user;
    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        user = new User("Max Mustermann", "test@example.com", "password");
        user.setId("user-id-123");
        portfolio = new Portfolio("Mein Portfolio", "Testbeschreibung", user);
        portfolio.setId("portfolio-id-456");
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("stock-id-789");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPortfolio_shouldUseAuthenticatedUser() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio savedPortfolio = invocation.getArgument(0);
            savedPortfolio.setId("new-portfolio-id");
            return savedPortfolio;
        });

        PortfolioCreateRequest request = new PortfolioCreateRequest("Neues Portfolio", "Beschreibung");

        // Act
        Portfolio created = portfolioService.createPortfolio(request);

        // Assert
        assertThat(created.getName()).isEqualTo("Neues Portfolio");
        assertThat(created.getUser().getEmail()).isEqualTo("test@example.com");
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findById_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        Optional<Portfolio> result = portfolioService.findById("portfolio-id-456");
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(portfolio);
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void findById_shouldFail_whenUserIsNotOwner() {
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        
        Optional<Portfolio> result = portfolioService.findById("portfolio-id-456");

        assertThat(result).isNotPresent();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findPortfoliosForCurrentUser_shouldSucceed() {
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(portfolioRepository.findByUserId("user-id-123")).thenReturn(Collections.singletonList(portfolio));

        List<Portfolio> result = portfolioService.findPortfoliosForCurrentUser();

        assertThat(result).containsExactly(portfolio);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addTransaction_shouldSucceed_whenUserIsOwner() {
        // Mocking repository calls
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Collections.singletonList(stock));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);

        // Creating the request DTO
        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockId("stock-id-789");
        request.setStockSymbol("AAPL");
        request.setQuantity(new BigDecimal("10"));
        request.setPricePerShare(new BigDecimal("150.00"));
        request.setTransactionDate(LocalDateTime.now());
        request.setTransactionType(TransactionType.BUY);

        // Calling the service method
        StockTransaction transaction = portfolioService.addStockTransaction("portfolio-id-456", request);

        // Asserting the results
        assertThat(transaction).isNotNull();
        assertThat(transaction.getStock()).isEqualTo(stock);
        // Verify that the transaction was added to the portfolio's list
        assertThat(portfolio.getTransactions()).contains(transaction);
        // Verify that the portfolio was saved, which cascades the save to the transaction
        verify(portfolioRepository).save(portfolio);
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void addTransaction_shouldFail_whenUserIsNotOwner() {
        when(portfolioRepository.findById("stock-id-789")).thenReturn(Optional.of(portfolio));
        StockTransactionRequest request = new StockTransactionRequest();

        assertThatThrownBy(() -> portfolioService.addStockTransaction("stock-id-789", request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockQuantity_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.isOwnerOfPortfolio("stock-id-789", "test@example.com")).thenReturn(true);
        when(portfolioRepository.findById("stock-id-789")).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findByPortfolioIdAndStockSymbol("stock-id-789", "AAPL")).thenReturn(List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), BigDecimal.ZERO,
                        TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("3"), BigDecimal.ZERO,
                        TransactionType.SELL)));

        BigDecimal quantity = portfolioService.getStockQuantity("stock-id-789", "AAPL");
        assertThat(quantity).isEqualByComparingTo("7");
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deletePortfolio_shouldSucceed_whenUserIsOwner() {
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        doNothing().when(portfolioRepository).delete(portfolio);

        portfolioService.deletePortfolio("portfolio-id-456");

        verify(portfolioRepository).delete(portfolio);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deletePortfolio_shouldThrowPortfolioNotFoundException_whenPortfolioDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(portfolioRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.deletePortfolio("non-existent-id"))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio with ID non-existent-id not found.");

        verify(portfolioRepository, never()).delete(any());
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void deletePortfolio_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        User anotherUser = new User("Jane Doe", "another@user.com", "password");
        when(userRepository.findByEmailIgnoreCase("another@user.com")).thenReturn(Optional.of(anotherUser));
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));

        assertThatThrownBy(() -> portfolioService.deletePortfolio("portfolio-id-456"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not authorized to delete this portfolio.");

        verify(portfolioRepository, never()).delete(any());
    }
}
