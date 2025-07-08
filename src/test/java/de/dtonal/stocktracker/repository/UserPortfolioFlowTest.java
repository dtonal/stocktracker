package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;

@DataJpaTest
@Tag("integration")
public class UserPortfolioFlowTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockTransactionRepository stockTransactionRepository;
    @Autowired
    private TestEntityManager entityManager; // Hilft beim Leeren des Caches

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        // Stellen Sie sicher, dass die Datenbank für jeden Test sauber ist.
        // @DataJpaTest rollt Transaktionen standardmäßig zurück, aber explizites Clear
        // kann helfen.
        entityManager.clear();
    }

    @Test
    void testTypicalUserPortfolioFlowAndCalculation() {
        // --- 1. User Anlegen ---
        String userEmail = "testuser@example.com";
        User user = new User("testuser", userEmail, passwordEncoder.encode("password"));
        user.setName("Alice");
        user.addRole(Role.USER);
        User savedUser = userRepository.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo(userEmail);
        System.out.println("User created: " + savedUser.getName());

        // --- 2. Portfolio für User anlegen ---
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My First Portfolio");
        portfolio.setDescription("Long-term investments");
        portfolio.setUser(savedUser); // Verknüpfe Portfolio mit User
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        assertThat(savedPortfolio).isNotNull();
        assertThat(savedPortfolio.getId()).isNotNull();
        assertThat(savedPortfolio.getUser().getId()).isEqualTo(savedUser.getId());
        System.out.println(
                "Portfolio created: " + savedPortfolio.getName() + " for user " + savedPortfolio.getUser().getName());

        // --- 3. Stocks hinzufügen ---
        Stock apple = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock microsoft = new Stock("MSFT", "Microsoft Corp.", "NASDAQ", "USD");

        Stock savedApple = stockRepository.save(apple);
        Stock savedMicrosoft = stockRepository.save(microsoft);
        assertThat(savedApple).isNotNull();
        assertThat(savedMicrosoft).isNotNull();
        System.out.println("Stocks added: " + savedApple.getSymbol() + ", " + savedMicrosoft.getSymbol());

        // --- 4. Mehrere Stocktransactions ---
        // Transaktion 1: AAPL Kauf
        StockTransaction tx1_apple_buy = new StockTransaction();
        tx1_apple_buy.setPortfolio(savedPortfolio);
        tx1_apple_buy.setStock(savedApple);
        tx1_apple_buy.setTransactionDate(LocalDate.of(2024, 1, 10).atStartOfDay());
        tx1_apple_buy.setQuantity(new BigDecimal("10"));
        tx1_apple_buy.setPricePerShare(new BigDecimal("170.50"));
        tx1_apple_buy.setTransactionType(TransactionType.BUY);
        stockTransactionRepository.save(tx1_apple_buy);
        System.out.println("Transaction 1: Buy 10 AAPL @ 170.50");

        // Transaktion 2: MSFT Kauf
        StockTransaction tx2_msft_buy = new StockTransaction();
        tx2_msft_buy.setPortfolio(savedPortfolio);
        tx2_msft_buy.setStock(savedMicrosoft);
        tx2_msft_buy.setTransactionDate(LocalDate.of(2024, 1, 15).atStartOfDay());
        tx2_msft_buy.setQuantity(new BigDecimal("5"));
        tx2_msft_buy.setPricePerShare(new BigDecimal("350.00"));
        tx2_msft_buy.setTransactionType(TransactionType.BUY);
        stockTransactionRepository.save(tx2_msft_buy);
        System.out.println("Transaction 2: Buy 5 MSFT @ 350.00");

        // Transaktion 3: AAPL weiterer Kauf
        StockTransaction tx3_apple_buy_more = new StockTransaction();
        tx3_apple_buy_more.setPortfolio(savedPortfolio);
        tx3_apple_buy_more.setStock(savedApple);
        tx3_apple_buy_more.setTransactionDate(LocalDate.of(2024, 2, 1).atStartOfDay());
        tx3_apple_buy_more.setQuantity(new BigDecimal("5"));
        tx3_apple_buy_more.setPricePerShare(new BigDecimal("175.00"));
        tx3_apple_buy_more.setTransactionType(TransactionType.BUY);
        stockTransactionRepository.save(tx3_apple_buy_more);
        System.out.println("Transaction 3: Buy 5 AAPL @ 175.00");

        // Transaktion 4: AAPL Verkauf (teilweise)
        StockTransaction tx4_apple_sell = new StockTransaction();
        tx4_apple_sell.setPortfolio(savedPortfolio);
        tx4_apple_sell.setStock(savedApple);
        tx4_apple_sell.setTransactionDate(LocalDate.of(2024, 3, 5).atStartOfDay());
        tx4_apple_sell.setQuantity(new BigDecimal("2")); // Verkauf von 2 Aktien
        tx4_apple_sell.setPricePerShare(new BigDecimal("180.00"));
        tx4_apple_sell.setTransactionType(TransactionType.SELL);
        stockTransactionRepository.save(tx4_apple_sell);
        System.out.println("Transaction 4: Sell 2 AAPL @ 180.00");

        entityManager.flush(); // Alle Transaktionen in die DB schreiben
        entityManager.clear(); // JPA-Cache leeren, um echte DB-Abfragen zu erzwingen

        // --- 5. Eine einfache Berechnung: Gesamtmenge einer Aktie im Portfolio ---
        // Um die aktuelle Menge an AAPL im Portfolio zu berechnen, müssen wir alle BUY-
        // und SELL-Transaktionen summieren.

        // Lade das Portfolio und die Transaktionen neu (um sicherzustellen, dass wir
        // frische Daten haben)
        Optional<Portfolio> reloadedPortfolioOpt = portfolioRepository.findById(savedPortfolio.getId());
        assertThat(reloadedPortfolioOpt).isPresent();
        Portfolio reloadedPortfolio = reloadedPortfolioOpt.get();

        // Lade die Apple-Aktie erneut, um sicherzustellen, dass wir die korrekte
        // Referenz haben
        Optional<Stock> reloadedAppleOpt = stockRepository.findById(savedApple.getId());
        assertThat(reloadedAppleOpt).isPresent();
        Stock reloadedApple = reloadedAppleOpt.get();

        // Finde alle Transaktionen für dieses Portfolio und diese Aktie
        List<StockTransaction> appleTransactions = stockTransactionRepository.findByPortfolioAndStock(reloadedPortfolio,
                reloadedApple);

        BigDecimal totalAppleQuantity = BigDecimal.ZERO;
        for (StockTransaction transaction : appleTransactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) {
                totalAppleQuantity = totalAppleQuantity.add(transaction.getQuantity());
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                totalAppleQuantity = totalAppleQuantity.subtract(transaction.getQuantity());
            }
        }

        System.out.println("Total AAPL quantity in portfolio: " + totalAppleQuantity);

        // --- Assertions für die Berechnung ---
        // Erwartet: (10 + 5) - 2 = 13
        assertThat(totalAppleQuantity).isEqualByComparingTo(new BigDecimal("13"));

        // Optional: Teste auch die Gesamtanzahl der Transaktionen
        assertThat(stockTransactionRepository.count()).isEqualTo(4);

        System.out.println("Integration test finished successfully.");
    }
}