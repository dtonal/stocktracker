package de.dtonal.stocktracker.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.dtonal.stocktracker.dto.CompanyProfile;
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
}
