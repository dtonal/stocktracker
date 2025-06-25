package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PortfolioServiceImplTest {

    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private User user;
    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("Max Mustermann", "max@example.com", "password");
        portfolio = new Portfolio("Mein Portfolio", "Testbeschreibung", user);
        portfolio.setUser(user);
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
    }

    @Test
    void testCreatePortfolio() {
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> {
            Portfolio p = invocation.getArgument(0);
            p.setUser(user);
            return p;
        });
        Portfolio created = portfolioService.createPortfolio("Mein Portfolio", "Testbeschreibung", user);
        assertThat(created.getName()).isEqualTo("Mein Portfolio");
        assertThat(created.getDescription()).isEqualTo("Testbeschreibung");
        assertThat(created.getUser()).isEqualTo(user);
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void testGetPortfolioById() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        Optional<Portfolio> result = portfolioService.getPortfolioById(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(portfolio);
    }

    @Test
    void testGetUserPortfolios() {
        List<Portfolio> portfolios = List.of(portfolio);
        when(portfolioRepository.findByUser(user)).thenReturn(portfolios);
        List<Portfolio> result = portfolioService.getUserPortfolios(user);
        assertThat(result).containsExactly(portfolio);
    }

    @Test
    void testAddStockTransaction() {
        portfolio.setUser(user);
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockTransaction transaction = portfolioService.addStockTransaction(
                1L, "AAPL", new BigDecimal("10"), new BigDecimal("150.00"), LocalDate.now(), "BUY");

        assertThat(transaction.getPortfolio()).isEqualTo(portfolio);
        assertThat(transaction.getStock()).isEqualTo(stock);
        assertThat(transaction.getQuantity()).isEqualTo(new BigDecimal("10"));
        assertThat(transaction.getPricePerShare()).isEqualTo(new BigDecimal("150.00"));
        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.BUY);
        verify(stockTransactionRepository).save(any(StockTransaction.class));
    }

    @Test
    void testGetCurrentStockQuantityInPortfolio() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        List<StockTransaction> transactions = List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), new BigDecimal("100.00"), TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("3"), new BigDecimal("110.00"), TransactionType.SELL)
        );
        when(stockTransactionRepository.findByPortfolioAndStock(portfolio, stock)).thenReturn(transactions);
        BigDecimal result = portfolioService.getCurrentStockQuantityInPortfolio(1L, "AAPL");
        assertThat(result).isEqualTo(new BigDecimal("7"));
    }

    @Test
    void testGetCurrentStockValueInPortfolio() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        List<StockTransaction> transactions = List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("5"), new BigDecimal("100.00"), TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("2"), new BigDecimal("120.00"), TransactionType.SELL),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("1"), new BigDecimal("130.00"), TransactionType.BUY)
        );
        when(stockTransactionRepository.findByPortfolioAndStock(portfolio, stock)).thenReturn(transactions);
        // Der letzte Preis ist 130.00, Menge: 4
        BigDecimal result = portfolioService.getCurrentStockValueInPortfolio(1L, "AAPL");
        assertThat(result).isEqualTo(new BigDecimal("4").multiply(new BigDecimal("130.00")));
    }

    @Test
    void testGetTotalPortfolioValue() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        List<StockTransaction> transactions = List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("5"), new BigDecimal("100.00"), TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("2"), new BigDecimal("120.00"), TransactionType.SELL)
        );
        when(stockTransactionRepository.findByPortfolio(portfolio)).thenReturn(transactions);
        BigDecimal result = portfolioService.getTotalPortfolioValue(1L);
        // 5*100 - 2*120 = 500 - 240 = 260
        assertThat(result).isEqualTo(new BigDecimal("260.00"));
    }

    @Test
    void testGetTotalStockValueInPortfolio() {
        // Diese Methode ruft getCurrentStockValueInPortfolio intern auf
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        List<StockTransaction> transactions = List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("5"), new BigDecimal("100.00"), TransactionType.BUY)
        );
        when(stockTransactionRepository.findByPortfolioAndStock(portfolio, stock)).thenReturn(transactions);
        BigDecimal result = portfolioService.getTotalStockValueInPortfolio(1L, "AAPL");
        assertThat(result).isEqualTo(new BigDecimal("5").multiply(new BigDecimal("100.00")));
    }
}
