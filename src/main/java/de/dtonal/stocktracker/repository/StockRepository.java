package de.dtonal.stocktracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.dtonal.stocktracker.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    /**
     * Findet eine Aktie anhand des Börsensymbols
     */
    List<Stock> findBySymbol(String symbol);

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