package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.HistoricalPriceRepository;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Tag("integration")
public class PortfolioCalculationServiceImplTest {

    @Autowired
    private PortfolioCalculationService portfolioCalculationService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;
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
    void getStockQuantity_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findByPortfolioIdAndStockSymbol("portfolio-id-456", "AAPL")).thenReturn(List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), BigDecimal.ZERO,
                        TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("3"), BigDecimal.ZERO,
                        TransactionType.SELL)));

        BigDecimal quantity = portfolioCalculationService.getStockQuantity("portfolio-id-456", "AAPL");
        assertThat(quantity).isEqualByComparingTo("7");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTotalPortfolioValue_shouldCalculateCorrectly() {
        // Arrange
        StockTransaction buyAapl = new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), BigDecimal.ZERO, TransactionType.BUY);
        StockTransaction sellAapl = new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("2"), BigDecimal.ZERO, TransactionType.SELL);
        StockTransaction buyGoog = new StockTransaction(stock2, portfolio, LocalDateTime.now(), new BigDecimal("5"), BigDecimal.ZERO, TransactionType.BUY);

        List<StockTransaction> allTransactions = List.of(buyAapl, sellAapl, buyGoog);

        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(stockTransactionRepository.findByPortfolioId("portfolio-id-456")).thenReturn(allTransactions);

        // Mocking for getStockQuantity
        when(stockTransactionRepository.findByPortfolioIdAndStockSymbol("portfolio-id-456", "AAPL"))
                .thenReturn(List.of(buyAapl, sellAapl));
        when(stockTransactionRepository.findByPortfolioIdAndStockSymbol("portfolio-id-456", "GOOG"))
                .thenReturn(List.of(buyGoog));

        // Mocking for prices
        HistoricalPrice aaplPrice = new HistoricalPrice(stock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice googPrice = new HistoricalPrice(stock2, LocalDate.now(), new BigDecimal("200.00"));

        when(historicalPriceRepository.findFirstByStockOrderByDateDesc(stock)).thenReturn(Optional.of(aaplPrice));
        when(historicalPriceRepository.findFirstByStockOrderByDateDesc(stock2)).thenReturn(Optional.of(googPrice));

        // Act
        BigDecimal totalValue = portfolioCalculationService.getTotalPortfolioValue("portfolio-id-456");

        // Assert
        // (10 - 2) * 150.00 = 8 * 150 = 1200
        // 5 * 200.00 = 1000
        // Total = 2200
        assertThat(totalValue).isEqualByComparingTo("2200.00");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTotalPortfolioValue_shouldReturnZero_forPortfolioWithNoTransactions() {
        // Arrange
        when(portfolioRepository.findById("portfolio-id-456")).thenReturn(Optional.of(portfolio));
        when(portfolioRepository.isOwnerOfPortfolio("portfolio-id-456", "test@example.com")).thenReturn(true);
        when(stockTransactionRepository.findByPortfolioId("portfolio-id-456")).thenReturn(Collections.emptyList());

        // Act
        BigDecimal totalValue = portfolioCalculationService.getTotalPortfolioValue("portfolio-id-456");

        // Assert
        assertThat(totalValue).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getTotalPortfolioValue_shouldThrowException_whenPortfolioNotFound() {
        // Arrange
        when(portfolioRepository.findById("non-existent-id")).thenReturn(Optional.empty());
        when(portfolioRepository.isOwnerOfPortfolio("non-existent-id", "test@example.com")).thenReturn(true);


        // Act & Assert
        assertThatThrownBy(() -> portfolioCalculationService.getTotalPortfolioValue("non-existent-id"))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio mit ID non-existent-id nicht gefunden");
    }
} 