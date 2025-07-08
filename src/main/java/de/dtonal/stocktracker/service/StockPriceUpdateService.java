package de.dtonal.stocktracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.dtonal.stocktracker.dto.PriceData;
import de.dtonal.stocktracker.model.HistoricalPrice;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.repository.HistoricalPriceRepository;
import de.dtonal.stocktracker.repository.StockRepository;

@Service
public class StockPriceUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(StockPriceUpdateService.class);
    private final StockRepository stockRepository;
    private final HistoricalPriceRepository historicalPriceRepository;
    private final StockDataService stockDataService;

    public StockPriceUpdateService(StockRepository stockRepository,
            HistoricalPriceRepository historicalPriceRepository,
            StockDataService stockDataService) {
        this.stockRepository = stockRepository;
        this.historicalPriceRepository = historicalPriceRepository;
        this.stockDataService = stockDataService;
    }

    @Scheduled(cron = "0 0 18 * * ?")
    public void updateAllStockPrices() {
        logger.info("Starting daily stock price update job.");
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            try {
                updateStockPrice(stock);
            } catch (Exception e) {
                logger.error("Could not update price for stock {}", stock.getSymbol(), e);
            }
        }
        logger.info("Finished daily stock price update job.");
    }

    public void updateStockPrice(Stock stock) {
        LocalDate today = LocalDate.now();

        Optional<HistoricalPrice> existingPrice = historicalPriceRepository.findByStockAndDate(stock, today);
        if (existingPrice.isPresent()) {
            logger.info("Price for stock {} for today already exists. Skipping.", stock.getSymbol());
            return;
        }

        logger.info("Fetching current price for stock {}", stock.getSymbol());
        Optional<PriceData> priceDataOptional = stockDataService.getLatestPriceData(stock.getSymbol());

        if (priceDataOptional.isPresent()) {
            BigDecimal currentPrice = priceDataOptional.get().getCurrentPrice();
            HistoricalPrice historicalPrice = new HistoricalPrice(stock, today, currentPrice);
            historicalPriceRepository.save(historicalPrice);
            logger.info("Successfully updated price for stock {} to {}", stock.getSymbol(), currentPrice);
        } else {
            logger.warn("Could not retrieve price for stock {}. It will be missing for today.", stock.getSymbol());
        }
    }
} 