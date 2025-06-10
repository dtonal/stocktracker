package de.dtonal.stocktracker.repository;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    /**
     * Findet alle Portfolios eines Benutzers
     */
    List<Portfolio> findByUser(User user);
    
    /**
     * Findet alle Portfolios eines Benutzers anhand der User-ID
     */
    List<Portfolio> findByUserId(Long userId);
    
    /**
     * Findet ein Portfolio anhand des Namens und des Benutzers
     */
    Portfolio findByNameAndUser(String name, User user);
    
    /**
     * Prüft, ob ein Portfolio mit diesem Namen für den Benutzer existiert
     */
    boolean existsByNameAndUser(String name, User user);
} 