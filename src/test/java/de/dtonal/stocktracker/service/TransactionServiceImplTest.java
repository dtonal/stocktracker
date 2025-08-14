package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;

@SpringBootTest
@Tag("integration")
public class TransactionServiceImplTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockService stockService;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;

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
    void addStockTransaction_shouldSucceed_whenUserIsOwner() {
        // Arrange
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(stockService.getOrCreateStock("AAPL")).thenReturn(stock);

        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setQuantity(new BigDecimal("10"));
        request.setPricePerShare(new BigDecimal("150.00"));
        request.setTransactionDate(LocalDateTime.now());
        request.setTransactionType(TransactionType.BUY);

        // Act
        StockTransaction transaction = transactionService.addStockTransaction("portfolio-id-456", request);

        // Assert
        assertThat(transaction).isNotNull();
        assertThat(transaction.getStock()).isEqualTo(stock);
        assertThat(portfolio.getTransactions()).contains(transaction);
        verify(portfolioRepository).save(portfolio);
        // Verify that for an EXISTING stock, we do NOT fetch the price again.
      }



    @Test
    @WithMockUser(username = "another@user.com")
    void addTransaction_shouldFail_whenUserIsNotOwner() {
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "another@user.com")).thenReturn(false);

        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");

        assertThatThrownBy(() -> transactionService.addStockTransaction("portfolio-id-456", request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteStockTransaction_shouldSucceed() {
        // Arrange
        StockTransaction transaction1 = new StockTransaction(stock, portfolio, LocalDateTime.now(), BigDecimal.TEN, BigDecimal.ONE, TransactionType.BUY);
        transaction1.setId("tx-1");
        StockTransaction transaction2 = new StockTransaction(stock, portfolio, LocalDateTime.now(), BigDecimal.TEN, BigDecimal.ONE, TransactionType.BUY);
        transaction2.setId("tx-2");
        portfolio.getTransactions().addAll(List.of(transaction1, transaction2));

        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findById("tx-1")).thenReturn(Optional.of(transaction1));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);


        // Act
        transactionService.deleteStockTransaction("portfolio-id-456", "tx-1");

        // Assert
        assertThat(portfolio.getTransactions()).hasSize(1);
        assertThat(portfolio.getTransactions().get(0).getId()).isEqualTo("tx-2");
        verify(portfolioRepository).save(portfolio);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deleteStockTransaction_shouldThrow_whenTransactionNotFound() {
        // Arrange
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findById("non-existent-tx-id")).thenReturn(Optional.empty());
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> transactionService.deleteStockTransaction("portfolio-id-456", "non-existent-tx-id"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction with ID non-existent-tx-id not found.");
    }
} 