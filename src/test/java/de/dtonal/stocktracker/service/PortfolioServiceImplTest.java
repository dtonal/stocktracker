package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class PortfolioServiceImplTest {

    @Autowired
    private PortfolioService portfolioService;

    @MockBean
    private PortfolioRepository portfolioRepository;
    @MockBean
    private StockRepository stockRepository;
    @MockBean
    private StockTransactionRepository stockTransactionRepository;

    private User user;
    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        user = new User("Max Mustermann", "test@example.com", "password");
        portfolio = new Portfolio("Mein Portfolio", "Testbeschreibung", user);
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
    }

    @Test
    void testCreatePortfolio() {
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(portfolio);
        Portfolio created = portfolioService.createPortfolio("Mein Portfolio", "Testbeschreibung", user);
        assertThat(created.getName()).isEqualTo("Mein Portfolio");
        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findById_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        Optional<Portfolio> result = portfolioService.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(portfolio);
    }

    @Test
    @WithMockUser(username = "another@user.com")
    void findById_shouldFail_whenUserIsNotOwner() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        assertThatThrownBy(() -> portfolioService.findById(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testFindByUser() {
        when(portfolioRepository.findByUser(user)).thenReturn(Collections.singletonList(portfolio));
        List<Portfolio> result = portfolioService.findByUser(user);
        assertThat(result).containsExactly(portfolio);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addTransaction_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockRepository.findBySymbol("AAPL")).thenReturn(Optional.of(stock));
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setQuantity(new BigDecimal("10"));
        request.setPricePerShare(new BigDecimal("150.00"));
        request.setTransactionDate(LocalDateTime.now());
        request.setTransactionType(TransactionType.BUY);

        StockTransaction transaction = portfolioService.addTransaction(1L, request);
        assertThat(transaction.getPortfolio()).isEqualTo(portfolio);
        assertThat(transaction.getStock()).isEqualTo(stock);
    }
    
    @Test
    @WithMockUser(username = "another@user.com")
    void addTransaction_shouldFail_whenUserIsNotOwner() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        StockTransactionRequest request = new StockTransactionRequest();
        
        assertThatThrownBy(() -> portfolioService.addTransaction(1L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getStockQuantity_shouldSucceed_whenUserIsOwner() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findByPortfolioIdAndStockSymbol(1L, "AAPL")).thenReturn(List.of(
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("10"), BigDecimal.ZERO, TransactionType.BUY),
                new StockTransaction(stock, portfolio, LocalDateTime.now(), new BigDecimal("3"), BigDecimal.ZERO, TransactionType.SELL)
        ));

        BigDecimal quantity = portfolioService.getStockQuantity(1L, "AAPL");
        assertThat(quantity).isEqualByComparingTo("7");
    }
}
