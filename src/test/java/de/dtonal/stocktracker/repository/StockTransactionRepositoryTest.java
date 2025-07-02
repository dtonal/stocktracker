package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;

@DataJpaTest
public class StockTransactionRepositoryTest {
    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PasswordEncoder passwordEncoder;
    private User testUser;
    private Portfolio testPortfolio;
    private Stock testStock;

    /**
     * Erstellt einen BigDecimal mit normalisierter Skalierung für Tests
     */
    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    /**
     * Vergleicht zwei BigDecimal-Werte mit normalisierter Skalierung
     */
    private static void assertBigDecimalEquals(BigDecimal actual, String expected) {
        BigDecimal expectedBd = new BigDecimal(expected);
        assertThat(actual.stripTrailingZeros()).isEqualTo(expectedBd.stripTrailingZeros());
    }

    /**
     * Vergleicht BigDecimal-Listen mit normalisierter Skalierung
     */
    private static void assertBigDecimalListEquals(List<BigDecimal> actual, String... expected) {
        assertThat(actual).extracting(BigDecimal::stripTrailingZeros)
                .containsExactlyInAnyOrder(
                        java.util.Arrays.stream(expected)
                                .map(BigDecimal::new)
                                .map(BigDecimal::stripTrailingZeros)
                                .toArray(BigDecimal[]::new));
    }

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        entityManager.clear();

        // Erstelle Testdaten
        testUser = new User("Test User", "test@example.com", passwordEncoder.encode("password"));
        userRepository.save(testUser);

        testPortfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(testPortfolio);

        testStock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(testStock);

        entityManager.flush();
    }

    @Test
    public void testSaveStockTransaction() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStock().getSymbol()).isEqualTo("AAPL");
        assertThat(found.get().getPortfolio().getName()).isEqualTo("Test Portfolio");
        assertBigDecimalEquals(found.get().getQuantity(), "10.5");
        assertThat(found.get().getPricePerShare()).isEqualTo(bd("150.00"));
        assertThat(found.get().getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testFindByPortfolio() {
        StockTransaction transaction1 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        StockTransaction transaction2 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("5.25"), bd("160.00"), TransactionType.SELL);
        stockTransactionRepository.saveAll(List.of(transaction1, transaction2));
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByPortfolio(testPortfolio);
        assertThat(found).hasSize(2);
        assertBigDecimalListEquals(
                found.stream().map(StockTransaction::getQuantity).toList(),
                "10.5", "5.25");
        assertThat(found).extracting(StockTransaction::getTransactionType)
                .containsExactlyInAnyOrder(TransactionType.BUY, TransactionType.SELL);
    }

    @Test
    public void testFindByPortfolioId() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByPortfolioId(testPortfolio.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPortfolio().getId()).isEqualTo(testPortfolio.getId());
    }

    @Test
    public void testFindByStock() {
        StockTransaction transaction1 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        StockTransaction transaction2 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("5.25"), bd("160.00"), TransactionType.SELL);
        stockTransactionRepository.saveAll(List.of(transaction1, transaction2));
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByStock(testStock);
        assertThat(found).hasSize(2);
        assertThat(found).extracting(StockTransaction::getStock).allMatch(stock -> stock.getSymbol().equals("AAPL"));
    }

    @Test
    public void testFindByStockId() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByStockId(testStock.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStock().getId()).isEqualTo(testStock.getId());
    }

    @Test
    public void testFindByPortfolioAndTransactionType() {
        StockTransaction buyTransaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        StockTransaction sellTransaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("5.25"), bd("160.00"), TransactionType.SELL);
        stockTransactionRepository.saveAll(List.of(buyTransaction, sellTransaction));
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> buyTransactions = stockTransactionRepository
                .findByPortfolioAndTransactionType(testPortfolio, TransactionType.BUY);
        List<StockTransaction> sellTransactions = stockTransactionRepository
                .findByPortfolioAndTransactionType(testPortfolio, TransactionType.SELL);

        assertThat(buyTransactions).hasSize(1);
        assertThat(sellTransactions).hasSize(1);
        assertThat(buyTransactions.get(0).getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(sellTransactions.get(0).getTransactionType()).isEqualTo(TransactionType.SELL);
    }

    @Test
    public void testFindByPortfolioAndTransactionDateBetween() {
        // Verwende einen festen Zeitstempel, um den Test deterministisch zu machen
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 12, 0, 0);
        LocalDateTime yesterday = now.minusDays(1).withHour(10).withMinute(0).withSecond(0);
        LocalDateTime today = now.withHour(14).withMinute(0).withSecond(0);
        LocalDateTime tomorrow = now.plusDays(1).withHour(10).withMinute(0).withSecond(0);
        LocalDateTime nextWeek = now.plusDays(7).withHour(10).withMinute(0).withSecond(0);

        StockTransaction transaction1 = new StockTransaction(
                testStock, testPortfolio, yesterday,
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        StockTransaction transaction2 = new StockTransaction(
                testStock, testPortfolio, today,
                bd("5.25"), bd("160.00"), TransactionType.SELL);
        StockTransaction transaction3 = new StockTransaction(
                testStock, testPortfolio, nextWeek,
                bd("3.75"), bd("170.00"), TransactionType.BUY);
        stockTransactionRepository.saveAll(List.of(transaction1, transaction2, transaction3));
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndTransactionDateBetween(
                testPortfolio, yesterday, tomorrow);

        assertThat(found).hasSize(2);
        assertBigDecimalListEquals(
                found.stream().map(StockTransaction::getQuantity).toList(),
                "10.5", "5.25");
    }

    @Test
    public void testUpdateStockTransaction() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        transaction.setQuantity(bd("15.75"));
        transaction.setPricePerShare(bd("155.00"));
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isPresent();
        assertBigDecimalEquals(found.get().getQuantity(), "15.75");
        assertThat(found.get().getPricePerShare()).isEqualTo(bd("155.00"));
        assertThat(found.get().getUpdatedAt()).isAfter(found.get().getCreatedAt());
    }

    @Test
    public void testDeleteStockTransaction() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        stockTransactionRepository.delete(transaction);
        entityManager.flush();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindAll() {
        StockTransaction transaction1 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        StockTransaction transaction2 = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("5.25"), bd("160.00"), TransactionType.SELL);
        stockTransactionRepository.saveAll(List.of(transaction1, transaction2));
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> all = stockTransactionRepository.findAll();
        assertThat(all).hasSize(2);
        assertBigDecimalListEquals(
                all.stream().map(StockTransaction::getQuantity).toList(),
                "10.5", "5.25");
    }

    @Test
    public void testFindAllEmpty() {
        List<StockTransaction> all = stockTransactionRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    public void testGetTotalValue() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTotalValue().stripTrailingZeros()).isEqualTo(bd("1575.00").stripTrailingZeros());
    }

    @Test
    public void testCreationDate() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getCreatedAt()).isCloseTo(found.get().getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    public void testUpdateDate() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime originalUpdatedAt = transaction.getUpdatedAt();

        transaction.setQuantity(bd("15.75"));
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        Optional<StockTransaction> found = stockTransactionRepository.findById(transaction.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    public void testFindByPortfolioEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByPortfolio(testPortfolio);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPortfolioIdEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByPortfolioId("999L");
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByStockEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByStock(testStock);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByStockIdEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByStockId("999L");
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPortfolioAndTransactionTypeEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndTransactionType(testPortfolio,
                TransactionType.BUY);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPortfolioAndTransactionDateBetweenEmpty() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime tomorrow = now.plusDays(1);

        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndTransactionDateBetween(
                testPortfolio, yesterday, tomorrow);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPortfolioAndStock() {
        StockTransaction transaction = new StockTransaction(
                testStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();

        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndStock(testPortfolio, testStock);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStock().getId()).isEqualTo(testStock.getId());
        assertThat(found.get(0).getPortfolio().getId()).isEqualTo(testPortfolio.getId());
    }

    @Test
    public void testFindByPortfolioAndStockEmpty() {
        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndStock(testPortfolio, testStock);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByPortfolioAndStockNotFound() {
        List<StockTransaction> found = stockTransactionRepository.findByPortfolioAndStock(testPortfolio, testStock);
        assertThat(found).isEmpty();

        Stock newStock = new Stock("MIC", "Microsoft.", "NASDAQ", "USD");
        stockRepository.save(newStock);
        StockTransaction transaction = new StockTransaction(
                newStock, testPortfolio, LocalDateTime.now(),
                bd("10.5"), bd("150.00"), TransactionType.BUY);
        stockTransactionRepository.save(transaction);
        entityManager.flush();
        entityManager.clear();
    }
}