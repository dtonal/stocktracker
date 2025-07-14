package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplUnitTest {
    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockPriceUpdateService stockPriceUpdateService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        portfolio.setId("p1");
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("s1");
    }

    @Test
    void findOrCreateStock_shouldReturnExistingStock_whenFound() {
        when(stockRepository.findBySymbol("AAPL")).thenReturn(List.of(stock));

        Stock result = transactionService.findOrCreateStock("AAPL");

        assertThat(result).isEqualTo(stock);
        verify(stockRepository, never()).save(any(Stock.class));
        verify(stockPriceUpdateService, never()).updateStockPrice(any(Stock.class));
    }

    @Test
    void findOrCreateStock_shouldCreateNewStock_whenNotFound() {
        Stock newStock = new Stock("TSLA", "Tesla", "NASDAQ", "USD");
        when(stockRepository.findBySymbol("TSLA")).thenReturn(Collections.emptyList());

        // We can't easily test the private method call, so we mock what it does
        TransactionServiceImpl spyService = spy(transactionService);
        doReturn(newStock).when(spyService).createNewStock("TSLA");

        Stock result = spyService.findOrCreateStock("TSLA");

        assertThat(result).isEqualTo(newStock);
        verify(spyService, times(1)).createNewStock("TSLA");
    }

    @Test
    void createNewStock_shouldSaveAndFetchPrice() {
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        doNothing().when(stockPriceUpdateService).updateStockPrice(stock);

        Stock result = transactionService.createNewStock("AAPL");

        assertThat(result).isEqualTo(stock);
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockPriceUpdateService, times(1)).updateStockPrice(stock);
    }

    @Test
    void createTransactionFromRequest_shouldMapAllFields() {
        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setTransactionType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("100"));
        request.setPricePerShare(new BigDecimal("123.45"));
        request.setTransactionDate(LocalDateTime.of(2023, 1, 1, 12, 0));

        StockTransaction result = transactionService.createTransactionFromRequest(stock, request);

        assertThat(result.getStock()).isEqualTo(stock);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.getQuantity()).isEqualByComparingTo("100");
        assertThat(result.getPricePerShare()).isEqualByComparingTo("123.45");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void isTransactionInPortfolio_shouldReturnTrue_whenTransactionBelongsToPortfolio() {
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(portfolio);

        assertThat(transactionService.isTransactionInPortfolio(transaction, portfolio)).isTrue();
    }

    @Test
    void isTransactionInPortfolio_shouldReturnFalse_whenTransactionDoesNotBelongToPortfolio() {
        Portfolio anotherPortfolio = new Portfolio();
        anotherPortfolio.setId("p2");
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(anotherPortfolio);

        assertThat(transactionService.isTransactionInPortfolio(transaction, portfolio)).isFalse();
    }
} 