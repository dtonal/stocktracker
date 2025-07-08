package de.dtonal.stocktracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.dtonal.stocktracker.dto.PriceData;
import de.dtonal.stocktracker.model.HistoricalPrice;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.repository.HistoricalPriceRepository;
import de.dtonal.stocktracker.repository.StockRepository;

@ExtendWith(MockitoExtension.class)
class StockPriceUpdateServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private HistoricalPriceRepository historicalPriceRepository;

    @Mock
    private StockDataService stockDataService;

    @InjectMocks
    private StockPriceUpdateService stockPriceUpdateService;

    @Captor
    private ArgumentCaptor<HistoricalPrice> historicalPriceCaptor;

    private Stock stock1;
    private Stock stock2;
    private PriceData priceData1;
    private PriceData priceData2;

    @BeforeEach
    void setUp() {
        stock1 = new Stock("AAPL", "Apple Inc.");
        stock2 = new Stock("GOOGL", "Alphabet Inc.");

        priceData1 = new PriceData();
        priceData1.setCurrentPrice(new BigDecimal("150.00"));

        priceData2 = new PriceData();
        priceData2.setCurrentPrice(new BigDecimal("2800.00"));
    }

    @Test
    void updateAllStockPrices_shouldFetchAndSavePrices_whenNoPricesForTodayExist() {
        // Arrange
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));
        when(historicalPriceRepository.findByStockAndDate(any(Stock.class), eq(LocalDate.now()))).thenReturn(Optional.empty());
        when(stockDataService.getLatestPriceData("AAPL")).thenReturn(Optional.of(priceData1));
        when(stockDataService.getLatestPriceData("GOOGL")).thenReturn(Optional.of(priceData2));

        // Act
        stockPriceUpdateService.updateAllStockPrices();

        // Assert
        verify(historicalPriceRepository, times(2)).save(historicalPriceCaptor.capture());
        
        List<HistoricalPrice> capturedPrices = historicalPriceCaptor.getAllValues();
        assertEquals(2, capturedPrices.size());
        
        // Check first saved price
        HistoricalPrice savedPrice1 = capturedPrices.stream().filter(p -> p.getStock().getSymbol().equals("AAPL")).findFirst().get();
        assertEquals("AAPL", savedPrice1.getStock().getSymbol());
        assertEquals(0, new BigDecimal("150.00").compareTo(savedPrice1.getClosingPrice()));

        // Check second saved price
        HistoricalPrice savedPrice2 = capturedPrices.stream().filter(p -> p.getStock().getSymbol().equals("GOOGL")).findFirst().get();
        assertEquals("GOOGL", savedPrice2.getStock().getSymbol());
        assertEquals(0, new BigDecimal("2800.00").compareTo(savedPrice2.getClosingPrice()));
    }

    @Test
    void updateAllStockPrices_shouldOnlyFetchPriceForStockWithoutRecentPrice() {
        // Arrange
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stock2));

        // This replaces the two fragile when() calls with one robust implementation.
        // It's immune to the null-pointer issues caused by Mockito's internal matching.
        when(historicalPriceRepository.findByStockAndDate(any(Stock.class), any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    Stock stock = invocation.getArgument(0);
                    // For stock "AAPL", we pretend a price already exists.
                    if (stock != null && "AAPL".equals(stock.getSymbol())) {
                        return Optional.of(new HistoricalPrice());
                    }
                    // For all other stocks (like "GOOGL"), no price exists.
                    return Optional.empty();
                });

        when(stockDataService.getLatestPriceData("GOOGL")).thenReturn(Optional.of(priceData2));

        // Act
        stockPriceUpdateService.updateAllStockPrices();

        // Assert
        verify(stockDataService, never()).getLatestPriceData("AAPL");
        verify(stockDataService, times(1)).getLatestPriceData("GOOGL");
        verify(historicalPriceRepository, times(1)).save(any(HistoricalPrice.class));
    }

    @Test
    void updateAllStockPrices_shouldContinueWhenApiFailsForOneStock() {
        // Arrange
        Stock stockFailing = new Stock("FAIL", "Failing Corp.");
        when(stockRepository.findAll()).thenReturn(List.of(stock1, stockFailing));
        when(historicalPriceRepository.findByStockAndDate(any(Stock.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        when(stockDataService.getLatestPriceData("AAPL")).thenReturn(Optional.of(priceData1));
        // API returns empty for the second stock
        when(stockDataService.getLatestPriceData("FAIL")).thenReturn(Optional.empty());
        
        // Act
        stockPriceUpdateService.updateAllStockPrices();

        // Assert
        verify(stockDataService, times(1)).getLatestPriceData("AAPL");
        verify(stockDataService, times(1)).getLatestPriceData("FAIL");
        // Only one save call should happen, for the successful one
        verify(historicalPriceRepository, times(1)).save(any(HistoricalPrice.class));
    }

    @Test
    void updateAllStockPrices_shouldDoNothingWhenNoStocksInDb() {
        // Arrange
        when(stockRepository.findAll()).thenReturn(List.of());

        // Act
        stockPriceUpdateService.updateAllStockPrices();

        // Assert
        verify(stockDataService, never()).getLatestPriceData(any());
        verify(historicalPriceRepository, never()).save(any());
    }
} 