package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockSearchResult;
import de.dtonal.stocktracker.model.Stock;

public interface StockService {
    Stock getOrCreateStock(String symbol);
    StockSearchResult searchStocks(String query);
}
