package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockSearchResult;
import de.dtonal.stocktracker.dto.StockSearchItem;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockDataService stockDataService;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock appleStock;
    private StockSearchItem microsoftStockItem;

    @BeforeEach
    void setUp() {
        appleStock = new Stock();
        appleStock.setSymbol("AAPL");
        appleStock.setName("Apple Inc.");

        microsoftStockItem = new StockSearchItem("Microsoft Corp", "MSFT", "MSFT", "Common Stock");
    }

    @Test
    @DisplayName("Test 1: Should return only local stock found by name")
    void search_whenOnlyLocalStockFoundByName_shouldReturnIt() {
        when(stockRepository.findByNameContainingIgnoreCase("Apple")).thenReturn(List.of(appleStock));
        when(stockRepository.findBySymbol(anyString())).thenReturn(Collections.emptyList());
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.empty());

        StockSearchResult results = stockService.searchStocks("Apple");

        assertThat(results.getResult()).hasSize(1);
        assertThat(results.getResult().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(results.getResult().get(0).isSavedInDb()).isTrue();
    }

    @Test
    @DisplayName("Test 2: Should return only local stock found by symbol")
    void search_whenOnlyLocalStockFoundBySymbol_shouldReturnIt() {
        when(stockRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(Collections.emptyList());
        when(stockRepository.findBySymbol("AAPL")).thenReturn(List.of(appleStock));
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.empty());

        StockSearchResult results = stockService.searchStocks("AAPL");

        assertThat(results.getResult()).hasSize(1);
        assertThat(results.getResult().get(0).getSymbol()).isEqualTo("AAPL");
        assertThat(results.getResult().get(0).isSavedInDb()).isTrue();
    }

    @Test
    @DisplayName("Test 3: Should return a single stock when found by both name and symbol")
    void search_whenStockFoundByMultipleLocalMethods_shouldReturnSingleEntry() {
        when(stockRepository.findByNameContainingIgnoreCase("Apple")).thenReturn(List.of(appleStock));
        when(stockRepository.findBySymbol("Apple")).thenReturn(List.of(appleStock));
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.empty());

        StockSearchResult results = stockService.searchStocks("Apple");

        assertThat(results.getResult()).hasSize(1);
    }

    @Test
    @DisplayName("Test 4: Should return only remote stock when no local results")
    void search_whenOnlyRemoteStockFound_shouldReturnIt() {
        when(stockRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(Collections.emptyList());
        when(stockRepository.findBySymbol(anyString())).thenReturn(Collections.emptyList());
        StockSearchResult finnhubResult = new StockSearchResult(1, List.of(microsoftStockItem));
        when(stockDataService.getStockSearchResult("MSFT")).thenReturn(Optional.of(finnhubResult));

        StockSearchResult results = stockService.searchStocks("MSFT");

        assertThat(results.getResult()).hasSize(1);
        assertThat(results.getResult().get(0).getSymbol()).isEqualTo("MSFT");
        assertThat(results.getResult().get(0).isSavedInDb()).isFalse();
    }

    @Test
    @DisplayName("Test 5: Should return combined list of local and new remote stocks")
    void search_whenLocalAndNewRemoteStocksFound_shouldReturnCombinedList() {
        when(stockRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(List.of(appleStock));
        when(stockRepository.findBySymbol(anyString())).thenReturn(Collections.emptyList());
        StockSearchResult finnhubResult = new StockSearchResult(1, List.of(microsoftStockItem));
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.of(finnhubResult));

        StockSearchResult results = stockService.searchStocks("A");

        assertThat(results.getResult()).hasSize(2);
        assertThat(results.getResult()).anyMatch(r -> r.getSymbol().equals("AAPL") && r.isSavedInDb());
        assertThat(results.getResult()).anyMatch(r -> r.getSymbol().equals("MSFT") && !r.isSavedInDb());
    }

    @Test
    @DisplayName("Test 6: Should merge results and ignore remote duplicate")
    void search_whenLocalAndOverlappingRemoteResults_shouldIgnoreRemoteDuplicate() {
        when(stockRepository.findBySymbol("A")).thenReturn(List.of(appleStock));

        StockSearchItem appleRemoteItem = new StockSearchItem("Apple Inc", "AAPL", "AAPL", "Common Stock");
        StockSearchResult finnhubResult = new StockSearchResult(2, List.of(appleRemoteItem, microsoftStockItem));
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.of(finnhubResult));

        StockSearchResult results = stockService.searchStocks("A");

        assertThat(results.getResult()).hasSize(2);
        
        // Ensure the Apple result is the one from DB
        Optional<StockSearchItem> appleResult = results.getResult().stream().filter(r -> r.getSymbol().equals("AAPL")).findFirst();
        assertThat(appleResult).isPresent();
        assertThat(appleResult.get().isSavedInDb()).isTrue();

        // Ensure Microsoft result is present and marked as not from DB
        assertThat(results.getResult()).anyMatch(r -> r.getSymbol().equals("MSFT") && !r.isSavedInDb());
    }

    @Test
    @DisplayName("Test 7: Should return empty list for null or blank query")
    void search_whenQueryIsNullorBlank_shouldReturnEmptyList() {
        // Test with null
        StockSearchResult resultsNull = stockService.searchStocks(null);
        assertThat(resultsNull.getResult()).isEmpty();

        // Test with blank
        StockSearchResult resultsBlank = stockService.searchStocks("   ");
        assertThat(resultsBlank.getResult()).isEmpty();

        // Verify no interactions with dependencies
        verifyNoInteractions(stockRepository, stockDataService);
    }


    @Test
    @DisplayName("Test 8: Should return empty list when no stocks are found")
    void search_whenNoResults_shouldReturnEmptyList() {
        when(stockRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(Collections.emptyList());
        when(stockRepository.findBySymbol(anyString())).thenReturn(Collections.emptyList());
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.empty());

        StockSearchResult results = stockService.searchStocks("NonExistent");

        assertThat(results.getResult()).isEmpty();
    }

    @Test
    @DisplayName("Test 9: Should handle empty optional from Finnhub service gracefully")
    void search_whenFinnhubReturnsEmptyOptional_shouldNotFail() {
        when(stockRepository.findBySymbol(anyString())).thenReturn(List.of(appleStock));
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.empty());

        StockSearchResult results = stockService.searchStocks("AAPL");

        assertThat(results.getResult()).hasSize(1);
        assertThat(results.getResult().get(0).getSymbol()).isEqualTo("AAPL");
    }

    @Test
    @DisplayName("Test 10: Should handle Finnhub result with empty list gracefully")
    void search_whenFinnhubReturnsEmptyResultList_shouldNotFail() {
        when(stockRepository.findBySymbol(anyString())).thenReturn(List.of(appleStock));
        StockSearchResult emptyFinnhubResult = new StockSearchResult(0, Collections.emptyList());
        when(stockDataService.getStockSearchResult(anyString())).thenReturn(Optional.of(emptyFinnhubResult));

        StockSearchResult results = stockService.searchStocks("AAPL");

        assertThat(results.getResult()).hasSize(1);
        assertThat(results.getResult().get(0).getSymbol()).isEqualTo("AAPL");
    }
}
