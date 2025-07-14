package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@Tag("integration")
public class TransactionServiceImplTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
    @MockBean
    private StockPriceUpdateService stockPriceUpdateService;

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
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Collections.singletonList(stock));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);

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
        verify(stockPriceUpdateService, never()).updateStockPrice(any(Stock.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addStockTransaction_shouldFetchPrice_whenStockIsNew() {
        // Arrange
        String newStockSymbol = "NVDA";
        Stock newStock = new Stock(newStockSymbol, "NVIDIA Corp", "NASDAQ", "USD");
        newStock.setId("new-stock-id");

        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol(newStockSymbol)).thenReturn(Collections.emptyList());
        when(stockRepository.save(any(Stock.class))).thenReturn(newStock);
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        doNothing().when(stockPriceUpdateService).updateStockPrice(any(Stock.class));

        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol(newStockSymbol);
        request.setQuantity(new BigDecimal("5"));
        request.setPricePerShare(new BigDecimal("900.00"));
        request.setTransactionDate(LocalDateTime.now());
        request.setTransactionType(TransactionType.BUY);

        // Act
        transactionService.addStockTransaction("portfolio-id-456", request);

        // Assert
        verify(stockRepository).save(argThat(s -> newStockSymbol.equals(s.getSymbol())));
        verify(stockPriceUpdateService, times(1)).updateStockPrice(newStock);
        verify(portfolioRepository).save(portfolio);
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