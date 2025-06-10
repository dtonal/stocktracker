package de.dtonal.stocktracker.repository;

import de.dtonal.stocktracker.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    /**
     * Findet eine Aktie anhand des Börsensymbols
     */
    Optional<Stock> findBySymbol(String symbol);
    
    /**
     * Prüft, ob eine Aktie mit diesem Symbol existiert
     */
    boolean existsBySymbol(String symbol);
    
    /**
     * Findet alle Aktien anhand der Börse
     */
    List<Stock> findByExchange(String exchange);
    
    /**
     * Findet alle Aktien anhand der Währung
     */
    List<Stock> findByCurrency(String currency);
    
    /**
     * Findet alle Aktien, deren Name den Suchbegriff enthält (case-insensitive)
     */
    List<Stock> findByNameContainingIgnoreCase(String name);
    
    /**
     * Findet alle Aktien anhand von Börse und Währung
     */
    List<Stock> findByExchangeAndCurrency(String exchange, String currency);
} 