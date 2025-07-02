package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import de.dtonal.stocktracker.model.Stock;

@DataJpaTest
public class StockRepositoryTest {
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        entityManager.clear();
    }

    @Test
    public void testSaveStock() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        Optional<Stock> found = stockRepository.findById(stock.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSymbol()).isEqualTo("AAPL");
        assertThat(found.get().getName()).isEqualTo("Apple Inc.");
        assertThat(found.get().getExchange()).isEqualTo("NASDAQ");
        assertThat(found.get().getCurrency()).isEqualTo("USD");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testFindBySymbol() {
        Stock stock = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        List<Stock> found = stockRepository.findBySymbol("MSFT");
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getSymbol()).isEqualTo("MSFT");
        assertThat(found.get(0).getName()).isEqualTo("Microsoft Corporation");
    }

    @Test
    public void testFindBySymbolNotFound() {
        List<Stock> found = stockRepository.findBySymbol("NONEXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    public void testExistsBySymbol() {
        Stock stock = new Stock("GOOGL", "Alphabet Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        boolean exists = stockRepository.existsBySymbol("GOOGL");
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsBySymbolNotFound() {
        boolean exists = stockRepository.existsBySymbol("NONEXISTENT");
        assertThat(exists).isFalse();
    }

    @Test
    public void testFindByExchange() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        Stock stock3 = new Stock("BMW", "BMW AG", "XTRA", "EUR");
        stockRepository.saveAll(List.of(stock1, stock2, stock3));
        entityManager.flush();
        entityManager.clear();

        List<Stock> nasdaqStocks = stockRepository.findByExchange("NASDAQ");
        assertThat(nasdaqStocks).hasSize(2);
        assertThat(nasdaqStocks).extracting(Stock::getSymbol).containsExactlyInAnyOrder("AAPL", "MSFT");
    }

    @Test
    public void testFindByCurrency() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        Stock stock3 = new Stock("BMW", "BMW AG", "XTRA", "EUR");
        stockRepository.saveAll(List.of(stock1, stock2, stock3));
        entityManager.flush();
        entityManager.clear();

        List<Stock> usdStocks = stockRepository.findByCurrency("USD");
        assertThat(usdStocks).hasSize(2);
        assertThat(usdStocks).extracting(Stock::getSymbol).containsExactlyInAnyOrder("AAPL", "MSFT");
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        Stock stock3 = new Stock("BMW", "BMW AG", "XTRA", "EUR");
        stockRepository.saveAll(List.of(stock1, stock2, stock3));
        entityManager.flush();
        entityManager.clear();

        List<Stock> appleStocks = stockRepository.findByNameContainingIgnoreCase("apple");
        assertThat(appleStocks).hasSize(1);
        assertThat(appleStocks.get(0).getSymbol()).isEqualTo("AAPL");

        List<Stock> microsoftStocks = stockRepository.findByNameContainingIgnoreCase("MICROSOFT");
        assertThat(microsoftStocks).hasSize(1);
        assertThat(microsoftStocks.get(0).getSymbol()).isEqualTo("MSFT");
    }

    @Test
    public void testFindByExchangeAndCurrency() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        Stock stock3 = new Stock("BMW", "BMW AG", "XTRA", "EUR");
        stockRepository.saveAll(List.of(stock1, stock2, stock3));
        entityManager.flush();
        entityManager.clear();

        List<Stock> nasdaqUsdStocks = stockRepository.findByExchangeAndCurrency("NASDAQ", "USD");
        assertThat(nasdaqUsdStocks).hasSize(2);
        assertThat(nasdaqUsdStocks).extracting(Stock::getSymbol).containsExactlyInAnyOrder("AAPL", "MSFT");
    }

    @Test
    public void testUpdateStock() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        stock.setName("Apple Inc. Updated");
        stock.setExchange("NYSE");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        Optional<Stock> found = stockRepository.findById(stock.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Apple Inc. Updated");
        assertThat(found.get().getExchange()).isEqualTo("NYSE");
        assertThat(found.get().getUpdatedAt()).isAfter(found.get().getCreatedAt());
    }

    @Test
    public void testDeleteStock() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        stockRepository.delete(stock);
        entityManager.flush();

        Optional<Stock> found = stockRepository.findById(stock.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        stockRepository.saveAll(List.of(stock1, stock2));
        entityManager.flush();
        entityManager.clear();

        List<Stock> all = stockRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Stock::getSymbol).containsExactlyInAnyOrder("AAPL", "MSFT");
    }

    @Test
    public void testFindAllEmpty() {
        List<Stock> all = stockRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    public void testCreationDate() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        Optional<Stock> found = stockRepository.findById(stock.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getCreatedAt()).isEqualTo(found.get().getUpdatedAt());
    }

    @Test
    public void testUpdateDate() {
        Stock stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime originalUpdatedAt = stock.getUpdatedAt();

        stock.setName("Apple Inc. Updated");
        stockRepository.save(stock);
        entityManager.flush();
        entityManager.clear();

        Optional<Stock> found = stockRepository.findById(stock.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    public void testCreateDuplicateSymbolOnDifferentExchangesAreValid() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock1);
        entityManager.flush();
        entityManager.clear();

        Stock stock2 = new Stock("AAPL", "Apple Inc. Duplicate", "NYSE", "USD");
        stockRepository.save(stock2);
        entityManager.flush();
        entityManager.clear();

        List<Stock> found = stockRepository.findBySymbol("AAPL");
        assertThat(found.size()).isEqualTo(2);
    }

    @Test
    public void testCreateDuplicateSymbolAndExchangeThrowsDataIntegrityViolationException() {
        Stock stock1 = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(stock1);
        entityManager.flush();
        entityManager.clear();

        Stock stock2 = new Stock("AAPL", "Apple Inc. Duplicate", "NASDAQ", "USD");
        assertThatThrownBy(() -> {
            stockRepository.save(stock2);
            entityManager.flush();
        })
                .hasRootCauseInstanceOf(JdbcSQLIntegrityConstraintViolationException.class);
    }

    @Test
    public void testFindByExchangeEmpty() {
        List<Stock> stocks = stockRepository.findByExchange("NONEXISTENT");
        assertThat(stocks).isEmpty();
    }

    @Test
    public void testFindByCurrencyEmpty() {
        List<Stock> stocks = stockRepository.findByCurrency("NONEXISTENT");
        assertThat(stocks).isEmpty();
    }

    @Test
    public void testFindByNameContainingIgnoreCaseEmpty() {
        List<Stock> stocks = stockRepository.findByNameContainingIgnoreCase("NONEXISTENT");
        assertThat(stocks).isEmpty();
    }

    @Test
    public void testFindByExchangeAndCurrencyEmpty() {
        List<Stock> stocks = stockRepository.findByExchangeAndCurrency("NONEXISTENT", "NONEXISTENT");
        assertThat(stocks).isEmpty();
    }
}