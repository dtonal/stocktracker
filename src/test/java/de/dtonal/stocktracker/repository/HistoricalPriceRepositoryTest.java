package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import de.dtonal.stocktracker.model.HistoricalPrice;
import de.dtonal.stocktracker.model.Stock;

@DataJpaTest
@Tag("integration")
public class HistoricalPriceRepositoryTest {
    @Autowired
    private HistoricalPriceRepository historicalPriceRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Stock testStock;

    @BeforeEach
    public void setUp() {
        entityManager.clear();

        testStock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stockRepository.save(testStock);
        entityManager.flush();
    }

    @Test
    public void testSaveHistoricalPrice() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findById(price.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getStock().getSymbol()).isEqualTo("AAPL");
        assertThat(found.get().getDate()).isEqualTo(LocalDate.now());
        assertThat(found.get().getClosingPrice()).isEqualTo(new BigDecimal("150.00"));
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testFindByStock() {
        HistoricalPrice price1 = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, LocalDate.now().minusDays(1), new BigDecimal("149.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> found = historicalPriceRepository.findByStock(testStock);
        assertThat(found).hasSize(2);
        assertThat(found).extracting(HistoricalPrice::getStock).allMatch(stock -> stock.getSymbol().equals("AAPL"));
    }

    @Test
    public void testFindByStockId() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> found = historicalPriceRepository.findByStockId(testStock.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStock().getId()).isEqualTo(testStock.getId());
    }

    @Test
    public void testFindByStockAndDateBetween() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate tomorrow = today.plusDays(1);

        HistoricalPrice price1 = new HistoricalPrice(testStock, twoDaysAgo, new BigDecimal("148.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, yesterday, new BigDecimal("149.00"));
        HistoricalPrice price3 = new HistoricalPrice(testStock, today, new BigDecimal("150.00"));
        HistoricalPrice price4 = new HistoricalPrice(testStock, tomorrow, new BigDecimal("151.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2, price3, price4));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> found = historicalPriceRepository.findByStockAndDateBetween(testStock, yesterday, today);
        assertThat(found).hasSize(2);
        assertThat(found).extracting(HistoricalPrice::getDate).containsExactlyInAnyOrder(yesterday, today);
    }

    @Test
    public void testFindFirstByStockOrderByDateDesc() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        HistoricalPrice price1 = new HistoricalPrice(testStock, yesterday, new BigDecimal("149.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, today, new BigDecimal("150.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2));
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findFirstByStockOrderByDateDesc(testStock);
        assertThat(found).isPresent();
        assertThat(found.get().getDate()).isEqualTo(today);
        assertThat(found.get().getClosingPrice()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    public void testFindFirstByStockOrderByDateAsc() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        HistoricalPrice price1 = new HistoricalPrice(testStock, yesterday, new BigDecimal("149.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, today, new BigDecimal("150.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2));
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findFirstByStockOrderByDateAsc(testStock);
        assertThat(found).isPresent();
        assertThat(found.get().getDate()).isEqualTo(yesterday);
        assertThat(found.get().getClosingPrice()).isEqualTo(new BigDecimal("149.00"));
    }

    @Test
    public void testFindByStockAndDate() {
        LocalDate specificDate = LocalDate.now();
        HistoricalPrice price = new HistoricalPrice(testStock, specificDate, new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findByStockAndDate(testStock, specificDate);
        assertThat(found).isPresent();
        assertThat(found.get().getDate()).isEqualTo(specificDate);
        assertThat(found.get().getClosingPrice()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    public void testFindByStockAndDateNotFound() {
        Optional<HistoricalPrice> found = historicalPriceRepository.findByStockAndDate(testStock, LocalDate.now());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindByClosingPriceGreaterThan() {
        HistoricalPrice price1 = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, LocalDate.now().minusDays(1), new BigDecimal("149.00"));
        HistoricalPrice price3 = new HistoricalPrice(testStock, LocalDate.now().minusDays(2), new BigDecimal("151.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2, price3));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> found = historicalPriceRepository.findByClosingPriceGreaterThan(new BigDecimal("149.50"));
        assertThat(found).hasSize(2);
        assertThat(found).extracting(HistoricalPrice::getClosingPrice)
                .allMatch(price -> price.compareTo(new BigDecimal("149.50")) > 0);
    }

    @Test
    public void testFindByClosingPriceLessThan() {
        HistoricalPrice price1 = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, LocalDate.now().minusDays(1), new BigDecimal("149.00"));
        HistoricalPrice price3 = new HistoricalPrice(testStock, LocalDate.now().minusDays(2), new BigDecimal("151.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2, price3));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> found = historicalPriceRepository.findByClosingPriceLessThan(new BigDecimal("150.50"));
        assertThat(found).hasSize(2);
        assertThat(found).extracting(HistoricalPrice::getClosingPrice)
                .allMatch(price -> price.compareTo(new BigDecimal("150.50")) < 0);
    }

    @Test
    public void testUpdateHistoricalPrice() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        price.setClosingPrice(new BigDecimal("155.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findById(price.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getClosingPrice()).isEqualTo(new BigDecimal("155.00"));
        assertThat(found.get().getUpdatedAt()).isAfter(found.get().getCreatedAt());
    }

    @Test
    public void testDeleteHistoricalPrice() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        historicalPriceRepository.delete(price);
        entityManager.flush();

        Optional<HistoricalPrice> found = historicalPriceRepository.findById(price.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindAll() {
        HistoricalPrice price1 = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice price2 = new HistoricalPrice(testStock, LocalDate.now().minusDays(1), new BigDecimal("149.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> all = historicalPriceRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(HistoricalPrice::getClosingPrice).containsExactlyInAnyOrder(
                new BigDecimal("150.00"), new BigDecimal("149.00"));
    }

    @Test
    public void testFindAllEmpty() {
        List<HistoricalPrice> all = historicalPriceRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    public void testCreationDate() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findById(price.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getCreatedAt()).isCloseTo(found.get().getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    public void testUpdateDate() {
        HistoricalPrice price = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime originalUpdatedAt = price.getUpdatedAt();

        price.setClosingPrice(new BigDecimal("155.00"));
        historicalPriceRepository.save(price);
        entityManager.flush();
        entityManager.clear();

        Optional<HistoricalPrice> found = historicalPriceRepository.findById(price.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    public void testFindByStockEmpty() {
        List<HistoricalPrice> found = historicalPriceRepository.findByStock(testStock);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByStockIdEmpty() {
        List<HistoricalPrice> found = historicalPriceRepository.findByStockId("999L");
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByStockAndDateBetweenEmpty() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        List<HistoricalPrice> found = historicalPriceRepository.findByStockAndDateBetween(testStock, yesterday,
                tomorrow);
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindFirstByStockOrderByDateDescEmpty() {
        Optional<HistoricalPrice> found = historicalPriceRepository.findFirstByStockOrderByDateDesc(testStock);
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindFirstByStockOrderByDateAscEmpty() {
        Optional<HistoricalPrice> found = historicalPriceRepository.findFirstByStockOrderByDateAsc(testStock);
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindByClosingPriceGreaterThanEmpty() {
        List<HistoricalPrice> found = historicalPriceRepository
                .findByClosingPriceGreaterThan(new BigDecimal("1000.00"));
        assertThat(found).isEmpty();
    }

    @Test
    public void testFindByClosingPriceLessThanEmpty() {
        List<HistoricalPrice> found = historicalPriceRepository.findByClosingPriceLessThan(new BigDecimal("1.00"));
        assertThat(found).isEmpty();
    }

    @Test
    public void testMultipleStocks() {
        Stock stock2 = new Stock("MSFT", "Microsoft Corporation", "NASDAQ", "USD");
        stockRepository.save(stock2);

        HistoricalPrice price1 = new HistoricalPrice(testStock, LocalDate.now(), new BigDecimal("150.00"));
        HistoricalPrice price2 = new HistoricalPrice(stock2, LocalDate.now(), new BigDecimal("300.00"));
        historicalPriceRepository.saveAll(List.of(price1, price2));
        entityManager.flush();
        entityManager.clear();

        List<HistoricalPrice> aaplPrices = historicalPriceRepository.findByStock(testStock);
        List<HistoricalPrice> msftPrices = historicalPriceRepository.findByStock(stock2);

        assertThat(aaplPrices).hasSize(1);
        assertThat(msftPrices).hasSize(1);
        assertThat(aaplPrices.get(0).getStock().getSymbol()).isEqualTo("AAPL");
        assertThat(msftPrices.get(0).getStock().getSymbol()).isEqualTo("MSFT");
    }
}