package de.dtonal.stocktracker.repository;

import de.dtonal.stocktracker.model.HistoricalPrice;
import de.dtonal.stocktracker.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalPriceRepository extends JpaRepository<HistoricalPrice, Long> {
    
    /**
     * Findet alle historischen Preise für eine bestimmte Aktie
     */
    List<HistoricalPrice> findByStock(Stock stock);
    
    /**
     * Findet alle historischen Preise für eine bestimmte Aktie anhand der Stock-ID
     */
    List<HistoricalPrice> findByStockId(Long stockId);
    
    /**
     * Findet alle historischen Preise für eine bestimmte Aktie in einem Datumsbereich
     */
    List<HistoricalPrice> findByStockAndDateBetween(Stock stock, LocalDate startDate, LocalDate endDate);
    
    /**
     * Findet den neuesten historischen Preis für eine bestimmte Aktie
     */
    Optional<HistoricalPrice> findFirstByStockOrderByDateDesc(Stock stock);
    
    /**
     * Findet den ältesten historischen Preis für eine bestimmte Aktie
     */
    Optional<HistoricalPrice> findFirstByStockOrderByDateAsc(Stock stock);
    
    /**
     * Findet einen spezifischen historischen Preis für eine Aktie an einem bestimmten Datum
     */
    Optional<HistoricalPrice> findByStockAndDate(Stock stock, LocalDate date);
    
    /**
     * Findet alle historischen Preise über einem bestimmten Wert
     */
    List<HistoricalPrice> findByClosingPriceGreaterThan(BigDecimal price);
    
    /**
     * Findet alle historischen Preise unter einem bestimmten Wert
     */
    List<HistoricalPrice> findByClosingPriceLessThan(BigDecimal price);
} 