package de.dtonal.stocktracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.PortfolioNotFoundException;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockTransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplUnitTest {
    @Mock
    private StockService stockService;
    @Mock
    private StockPriceUpdateService stockPriceUpdateService;
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio();
        portfolio.setId("p1");
        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("s1");
    }

    @Test
    void createTransactionFromRequest_shouldMapAllFields() {
        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setTransactionType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("100"));
        request.setPricePerShare(new BigDecimal("123.45"));
        request.setTransactionDate(LocalDateTime.of(2023, 1, 1, 12, 0));

        StockTransaction result = transactionService.createTransactionFromRequest(stock, request);

        assertThat(result.getStock()).isEqualTo(stock);
        assertThat(result.getTransactionType()).isEqualTo(TransactionType.BUY);
        assertThat(result.getQuantity()).isEqualByComparingTo("100");
        assertThat(result.getPricePerShare()).isEqualByComparingTo("123.45");
        assertThat(result.getTransactionDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void isTransactionInPortfolio_shouldReturnTrue_whenTransactionBelongsToPortfolio() {
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(portfolio);

        assertThat(transactionService.isTransactionInPortfolio(transaction, portfolio)).isTrue();
    }

    @Test
    void isTransactionInPortfolio_shouldReturnFalse_whenTransactionDoesNotBelongToPortfolio() {
        Portfolio anotherPortfolio = new Portfolio();
        anotherPortfolio.setId("p2");
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(anotherPortfolio);

        assertThat(transactionService.isTransactionInPortfolio(transaction, portfolio)).isFalse();
    }

    @Test
    void deleteStockTransaction_shouldDeleteTransactionFromPortfolio() {
        StockTransaction transaction = new StockTransaction();
        transaction.setPortfolio(portfolio);
        transaction.setId("t1");

        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        transactionService.deleteStockTransaction(portfolio.getId(), transaction.getId());

        assertThat(portfolio.getTransactions()).isEmpty();
    }

    @Test
    void deleteStockTransaction_shouldThrowException_whenTransactionDoesNotExist() {
        StockTransaction transaction = new StockTransaction();

        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findById(transaction.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteStockTransaction(portfolio.getId(), transaction.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction with ID " + transaction.getId() + " not found.");
    }

    @Test
    void addStockTransaction_shouldThrowException_whenPortfolioDoesNotExist() {
        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setTransactionType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("100"));
        request.setPricePerShare(new BigDecimal("123.45"));
        request.setTransactionDate(LocalDateTime.of(2023, 1, 1, 12, 0));

        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addStockTransaction(portfolio.getId(), request))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessage("Portfolio not found with id: " + portfolio.getId());
    }

    @Test
    void addStockTransaction_shouldAddTransactionToPortfolio() {


        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setTransactionType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("100"));
        request.setPricePerShare(new BigDecimal("123.45"));
        request.setTransactionDate(LocalDateTime.of(2023, 1, 1, 12, 0));
        
        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));
        when(stockService.getOrCreateStock(request.getStockSymbol())).thenReturn(stock);
        when(portfolioRepository.save(portfolio)).thenReturn(portfolio);

        transactionService.addStockTransaction(portfolio.getId(), request);

        assertThat(portfolio.getTransactions()).hasSize(1);
        assertThat(portfolio.getTransactions().get(0).getStock()).isEqualTo(stock);
        assertThat(portfolio.getTransactions().get(0).getTransactionType()).isEqualTo(TransactionType.BUY); 
        assertThat(portfolio.getTransactions().get(0).getQuantity()).isEqualByComparingTo("100");
        assertThat(portfolio.getTransactions().get(0).getPricePerShare()).isEqualByComparingTo("123.45");
        assertThat(portfolio.getTransactions().get(0).getTransactionDate()).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 0));
    }

    @Test
    void deleteStockTransaction_shouldThrowException_whenTransactionDoesNotBelongToPortfolio() {
        StockTransaction transaction = new StockTransaction();
        Portfolio anotherPortfolio = new Portfolio();
        anotherPortfolio.setId("p2");
        transaction.setPortfolio(anotherPortfolio);
        transaction.setId("t1");

        when(portfolioRepository.findById(portfolio.getId())).thenReturn(Optional.of(portfolio));
        when(stockTransactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> transactionService.deleteStockTransaction(portfolio.getId(), transaction.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction with ID " + transaction.getId() + " does not belong to portfolio with ID " + portfolio.getId());
    }
} 