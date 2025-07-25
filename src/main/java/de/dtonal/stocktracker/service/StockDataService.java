package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.PriceData;

import java.util.Optional;

public interface StockDataService {
    Optional<PriceData> getLatestPriceData(String symbol);
} 