package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.FinnhubQuote;
import de.dtonal.stocktracker.dto.PriceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class FinnhubStockDataService implements StockDataService {

    private static final Logger logger = LoggerFactory.getLogger(FinnhubStockDataService.class);

    private final RestTemplate restTemplate;

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.url}")
    private String apiUrl;

    public FinnhubStockDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<PriceData> getLatestPriceData(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String url = String.format("%s/quote?symbol=%s&token=%s", apiUrl, symbol, apiKey);

        try {
            FinnhubQuote quote = restTemplate.getForObject(url, FinnhubQuote.class);

            if (quote == null || quote.getCurrentPrice() == null || quote.getCurrentPrice().doubleValue() == 0.0) {
                logger.warn("Finnhub returned no or invalid data for symbol: {}", symbol);
                return Optional.empty();
            }

            PriceData priceData = new PriceData();
            priceData.setCurrentPrice(quote.getCurrentPrice());
            priceData.setChange(quote.getChange());
            priceData.setPercentChange(quote.getPercentChange());
            priceData.setHighPriceOfDay(quote.getHighPriceOfDay());
            priceData.setLowPriceOfDay(quote.getLowPriceOfDay());
            priceData.setOpenPriceOfDay(quote.getOpenPriceOfDay());
            priceData.setPreviousClosePrice(quote.getPreviousClosePrice());

            return Optional.of(priceData);

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching data for symbol {}: {} - {}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("An unexpected error occurred fetching data for symbol {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }
} 