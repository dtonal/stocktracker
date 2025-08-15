package de.dtonal.stocktracker.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.dtonal.stocktracker.dto.CompanyProfile;
import de.dtonal.stocktracker.dto.StockSearchItem;
import de.dtonal.stocktracker.dto.StockSearchResult;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockDataService stockDataService;
    private final StockRepository stockRepository;
    private final StockPriceUpdateService stockPriceUpdateService;

    public Stock getOrCreateStock(String stockSymbol) {
        return stockRepository.findBySymbol(stockSymbol)
            .stream()
            .findFirst()
            .orElseGet(() -> createNewStock(stockSymbol));
    }

    Stock createNewStock(String stockSymbol) {
        Optional<CompanyProfile> profile = stockDataService.getStockProfile(stockSymbol);
        if (profile.isEmpty()) {
            throw new IllegalArgumentException("Stock with symbol " + stockSymbol + " not found.");
        }
        CompanyProfile companyProfile = profile.get();
        Stock newStock = new Stock(
            stockSymbol,
            companyProfile.getName(),
            companyProfile.getExchange(),
            companyProfile.getCurrency()
        );
        Stock savedStock = stockRepository.save(newStock);
        
        log.info("New stock {} created, fetching initial price.", savedStock.getSymbol());
        stockPriceUpdateService.updateStockPrice(savedStock);
        return savedStock;
    }

    @Override
    public StockSearchResult searchStocks(String query) {
        if (query == null || query.isBlank()) {
            return new StockSearchResult(0, List.of());
        }
        Set<Stock> localStocks = new HashSet<>();
        localStocks.addAll(stockRepository.findBySymbol(query));
        localStocks.addAll(stockRepository.findByNameContainingIgnoreCase(query));

        Set<String> localSymbols = localStocks.stream()
            .map(Stock::getSymbol)
            .collect(Collectors.toSet());

        List<StockSearchItem> combinedResults = localStocks.stream()
                .map(stock -> StockSearchItem.builder()
                    .symbol(stock.getSymbol())
                    .description(stock.getName())
                    .displaySymbol(stock.getSymbol())
                    .type("stock")
                    .isSavedInDb(true)
                    .build())
            .collect(Collectors.toList());

        Optional<StockSearchResult> remoteResults = stockDataService.getStockSearchResult(query);

        if (remoteResults.isPresent()) {
            remoteResults.get().getResult().forEach(remoteResult -> {
                if (!localSymbols.contains(remoteResult.getSymbol())) {
                    combinedResults.add(remoteResult);
                }
            });
        }
    
        return new StockSearchResult(combinedResults.size(), combinedResults);
    }
}
