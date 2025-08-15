package de.dtonal.stocktracker.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import de.dtonal.stocktracker.dto.CompanyProfile;
import de.dtonal.stocktracker.dto.FinnhubQuote;
import de.dtonal.stocktracker.dto.StockSearchResult;
import de.dtonal.stocktracker.dto.PriceData;

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
        logger.info("Fetching latest price data for symbol: {}", url);

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

    @Override
    public Optional<CompanyProfile> getStockProfile(String isin) {
        if (isin == null || isin.isBlank()) {
            return Optional.empty();
        }

        Optional<String> symbol = getStockSymbol(isin);

        if (symbol.isEmpty()) {
            return Optional.empty();
        }

        String url = String.format("%s/stock/profile2?symbol=%s&token=%s", apiUrl, symbol.get(), apiKey);

        try {
            CompanyProfile profile = restTemplate.getForObject(url, CompanyProfile.class);
            if (profile == null) {
                logger.warn("Finnhub returned no data for isin: {}", isin);
                return Optional.empty();
            }
            profile.setIsin(isin);
            return Optional.of(profile);

        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching data for isin {}: {} - {}", isin, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("An unexpected error occurred fetching data for isin {}: {}", isin, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<StockSearchResult> getStockSearchResult(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        String url = String.format("%s/search?q=%s&exchange=US&token=%s", apiUrl, query, apiKey);

        try {
            StockSearchResult searchResult = restTemplate.getForObject(url, StockSearchResult.class);
            if (searchResult == null || searchResult.getCount() == 0 || searchResult.getResult() == null || searchResult.getResult().isEmpty()) {
                logger.warn("Finnhub returned no data for query: {}", query);
                return Optional.empty();
            }
            return Optional.of(searchResult);
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching data for query {}: {} - {}", query, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("An unexpected error occurred fetching data for query {}: {}", query, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> getStockSymbol(String isin) {
        if (isin == null || isin.isBlank()) {
            return Optional.empty();
        }
    
        String url = String.format("%s/search?q=%s&token=%s", apiUrl, isin, apiKey);
    
        try {
            StockSearchResult searchResult = restTemplate.getForObject(url, StockSearchResult.class);
            if (searchResult != null && searchResult.getResult() != null && !searchResult.getResult().isEmpty()) {
                return Optional.of(searchResult.getResult().get(0).getSymbol());
            }
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching data for isin {}: {} - {}", isin, e.getStatusCode(), e.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("An unexpected error occurred fetching data for isin {}: {}", isin, e.getMessage(), e);
            return Optional.empty();
        }
    }
} 