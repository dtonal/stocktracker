package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.HistoricalPriceRepository;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioCalculationServiceImpl implements PortfolioCalculationService {

    private final PortfolioRepository portfolioRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final HistoricalPriceRepository historicalPriceRepository;

    @Override
    @PreAuthorize("@portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name) or hasRole('ADMIN')")
    public BigDecimal getTotalPortfolioValue(String portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio mit ID " + portfolioId + " nicht gefunden"));

        List<StockTransaction> transactions = stockTransactionRepository.findByPortfolioId(portfolio.getId());

        Set<Stock> stocksInPortfolio = transactions.stream()
                .map(StockTransaction::getStock)
                .collect(Collectors.toSet());

        Map<Stock, BigDecimal> stockQuantities = calculateStockQuantities(portfolio.getId(), stocksInPortfolio);

        Map<Stock, BigDecimal> latestPrices = fetchLatestPrices(stockQuantities.keySet());

        return calculateTotalValue(stockQuantities, latestPrices);
    }

    @Override
    @PreAuthorize("@portfolioRepository.isOwnerOfPortfolio(#portfolioId, authentication.name) or hasRole('ADMIN')")
    public BigDecimal getStockQuantity(String portfolioId, String stockSymbol) {
        // First, check if portfolio exists and user has access
        portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio mit ID " + portfolioId + " nicht gefunden"));

        List<StockTransaction> transactions = stockTransactionRepository.findByPortfolioIdAndStockSymbol(portfolioId, stockSymbol);
        return calculateStockQuantity(transactions);
    }

    // --- Helper methods for pure logic ---

    BigDecimal calculateTotalValue(Map<Stock, BigDecimal> stockQuantities, Map<Stock, BigDecimal> latestPrices) {
        return stockQuantities.entrySet().stream()
                .map(entry -> {
                    Stock stock = entry.getKey();
                    BigDecimal quantity = entry.getValue();
                    BigDecimal price = latestPrices.getOrDefault(stock, BigDecimal.ZERO);
                    BigDecimal value = price.multiply(quantity);
                    log.info("Stock: {}, Quantity: {}, Price: {}, Value: {}", stock.getSymbol(), quantity, price, value);
                    return value;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    BigDecimal calculateStockQuantity(List<StockTransaction> transactions) {
        BigDecimal quantity = BigDecimal.ZERO;
        for (StockTransaction t : transactions) {
            if (t.getTransactionType() == TransactionType.BUY) {
                quantity = quantity.add(t.getQuantity());
            } else {
                quantity = quantity.subtract(t.getQuantity());
            }
        }
        return quantity;
    }

    // --- Helper methods with dependencies ---

    private Map<Stock, BigDecimal> calculateStockQuantities(String portfolioId, Set<Stock> stocks) {
        return stocks.stream().collect(Collectors.toMap(
                s -> s,
                s -> getStockQuantity(portfolioId, s.getSymbol())
        ));
    }

    private Map<Stock, BigDecimal> fetchLatestPrices(Set<Stock> stocks) {
        return stocks.stream()
                .collect(Collectors.toMap(
                        stock -> stock,
                        stock -> historicalPriceRepository.findFirstByStockOrderByDateDesc(stock)
                                .map(HistoricalPrice::getClosingPrice)
                                .orElse(BigDecimal.ZERO)
                ));
    }
} 