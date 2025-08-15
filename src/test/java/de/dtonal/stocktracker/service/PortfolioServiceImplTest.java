package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.PortfolioUpdateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.PortfolioNotFoundException;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.HistoricalPriceRepository;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import de.dtonal.stocktracker.repository.UserRepository;

@SpringBootTest
@Tag("integration")
class PortfolioServiceImplTest {

    @Autowired
    private PortfolioService portfolioService;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private PortfolioCalculationService portfolioCalculationService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private HistoricalPriceRepository historicalPriceRepository;

    private User user;
    private Portfolio portfolio;
    private Stock stock;
    private Stock stock2;

    @BeforeEach
    void setUp() {
        user = new User("Max Mustermann", "test@example.com", "password");
        user.setId("user-id-123");
        portfolio = new Portfolio("Mein Portfolio", "Testbeschreibung", user);
        portfolio.setId("portfolio-id-456");
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("stock-id-789");
        stock2 = new Stock("GOOG", "Google LLC", "NASDAQ", "USD");
        stock2.setId("stock-id-987");
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
    void addStockTransaction_shouldDelegateToTransactionService() {
        StockTransactionRequest request = new StockTransactionRequest();
        portfolioService.addStockTransaction("portfolio-id-456", request);
        verify(transactionService).addStockTransaction("portfolio-id-456", request);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteStockTransaction_shouldDelegateToTransactionService() {
        portfolioService.deleteStockTransaction("portfolio-id-456", "tx-1");
        verify(transactionService).deleteStockTransaction("portfolio-id-456", "tx-1");
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void addTransaction_shouldFail_whenUserIsNotOwner() {
        StockTransactionRequest request = new StockTransactionRequest();

        doThrow(new AccessDeniedException("")).when(transactionService).addStockTransaction("stock-id-789", request);

        assertThatThrownBy(() -> portfolioService.addStockTransaction("stock-id-789", request))
                .isInstanceOf(AccessDeniedException.class);
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

    @Test
    @WithMockUser(username = "test@example.com")
    void updatePortfolio_shouldSucceed_whenUserIsOwner() {
        // Arrange
        PortfolioUpdateRequest updateRequest = new PortfolioUpdateRequest();
        updateRequest.setName("Updated Portfolio Name");
        updateRequest.setDescription("Updated Description");

        // Mock the findById to return the portfolio, simulating the user is the owner
        when(portfolioService.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Portfolio updatedPortfolio = portfolioService.updatePortfolio("portfolio-id-456", updateRequest);

        // Assert
        assertThat(updatedPortfolio.getName()).isEqualTo("Updated Portfolio Name");
        assertThat(updatedPortfolio.getDescription()).isEqualTo("Updated Description");
        verify(portfolioRepository).save(portfolio);
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void updatePortfolio_shouldThrowNotFound_whenUserIsNotOwner() {
        // Arrange
        PortfolioUpdateRequest updateRequest = new PortfolioUpdateRequest();
        updateRequest.setName("Malicious Update");

        // Mock the findById to return empty, simulating a failed security check
        when(portfolioService.findById("portfolio-id-456")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.updatePortfolio("portfolio-id-456", updateRequest))
                .isInstanceOf(PortfolioNotFoundException.class);
        verify(portfolioRepository, never()).save(any(Portfolio.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updatePortfolio_shouldThrowNotFound_whenPortfolioDoesNotExist() {
        // Arrange
        PortfolioUpdateRequest updateRequest = new PortfolioUpdateRequest();
        updateRequest.setName("Update for Non-Existent");

        when(portfolioService.findById("non-existent-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.updatePortfolio("non-existent-id", updateRequest))
                .isInstanceOf(PortfolioNotFoundException.class);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTotalPortfolioValue_shouldDelegateToCalculationService() {
        portfolioService.getTotalPortfolioValue("portfolio-id-456");
        verify(portfolioCalculationService).getTotalPortfolioValue("portfolio-id-456");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockQuantity_shouldDelegateToCalculationService() {
        portfolioService.getStockQuantity("portfolio-id-456", "AAPL");
        verify(portfolioCalculationService).getStockQuantity("portfolio-id-456", "AAPL");
    }
}
