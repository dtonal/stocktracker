package de.dtonal.stocktracker.service;

import java.math.BigDecimal;

public interface PortfolioCalculationService {
    BigDecimal getTotalPortfolioValue(String portfolioId);
    BigDecimal getStockQuantity(String portfolioId, String stockSymbol);
} 