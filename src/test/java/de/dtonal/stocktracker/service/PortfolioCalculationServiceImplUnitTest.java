package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationServiceImplUnitTest {

    @InjectMocks
    private PortfolioCalculationServiceImpl portfolioCalculationService;

    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio("Test Portfolio", "Description", null);
        portfolio.setId("p1");

        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("s1");
    }

    @Test
    void calculateStockQuantity_shouldCorrectlySumBuyAndSell() {
        StockTransaction buy = new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), BigDecimal.ZERO, TransactionType.BUY);
        StockTransaction sell = new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("3"), BigDecimal.ZERO, TransactionType.SELL);
        StockTransaction buyAgain = new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("5"), BigDecimal.ZERO, TransactionType.BUY);

        List<StockTransaction> transactions = List.of(buy, sell, buyAgain);

        BigDecimal result = portfolioCalculationService.calculateStockQuantity(transactions);

        assertThat(result).isEqualByComparingTo("12");
    }

    @Test
    void calculateStockQuantity_shouldReturnZero_whenNoTransactions() {
        BigDecimal result = portfolioCalculationService.calculateStockQuantity(Collections.emptyList());
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateTotalValue_shouldCalculateCorrectly() {
        Stock stock2 = new Stock("GOOG", "Google", "NASDAQ", "USD");
        stock2.setId("s2");
        Map<Stock, BigDecimal> quantities = Map.of(
                stock, new BigDecimal("10"),
                stock2, new BigDecimal("5")
        );
        Map<Stock, BigDecimal> prices = Map.of(
                stock, new BigDecimal("150.00"),
                stock2, new BigDecimal("2000.00")
        );

        BigDecimal totalValue = portfolioCalculationService.calculateTotalValue(quantities, prices);

        // (10 * 150) + (5 * 2000) = 1500 + 10000 = 11500
        assertThat(totalValue).isEqualByComparingTo("11500.00");
    }

    @Test
    void calculateTotalValue_shouldReturnZero_whenPriceIsMissing() {
        Map<Stock, BigDecimal> quantities = Map.of(stock, new BigDecimal("10"));
        Map<Stock, BigDecimal> prices = Collections.emptyMap(); // Price for stock is missing

        BigDecimal totalValue = portfolioCalculationService.calculateTotalValue(quantities, prices);

        assertThat(totalValue).isEqualByComparingTo(BigDecimal.ZERO);
    }
} 