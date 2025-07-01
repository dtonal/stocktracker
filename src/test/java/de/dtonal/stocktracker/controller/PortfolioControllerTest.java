package de.dtonal.stocktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dtonal.stocktracker.config.ApplicationConfig;
import de.dtonal.stocktracker.config.JwtAuthFilter;
import de.dtonal.stocktracker.config.SecurityConfig;
import de.dtonal.stocktracker.config.TestSecurityConfig;
import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.*;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.PortfolioService;
import de.dtonal.stocktracker.service.UserService;
import de.dtonal.stocktracker.service.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PortfolioController.class)
@Import({ SecurityConfig.class, ApplicationConfig.class })
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private UserServiceImpl userService; // Konkrete Implementierung statt Interface

    @MockBean
    private JwtService jwtService;

    private User testUser;
    private Portfolio testPortfolio;
    private Stock testStock;
    private StockTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setRoles(Set.of(Role.USER));

        testStock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");

        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setDescription("Test Description");
        testPortfolio.setUser(testUser);

        testTransaction = new StockTransaction(testStock, testPortfolio, LocalDateTime.now(),
                BigDecimal.TEN, BigDecimal.valueOf(150.00), TransactionType.BUY);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreatePortfolio_Success() throws Exception {
        PortfolioCreateRequest request = new PortfolioCreateRequest();
        request.setName("Test Portfolio");
        request.setDescription("Test Description");

        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.createPortfolio(eq("Test Portfolio"), eq("Test Description"), eq(testUser)))
                .thenReturn(testPortfolio);

        mockMvc.perform(post("/api/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testPortfolio.getId()))
                .andExpect(jsonPath("$.name").value(testPortfolio.getName()))
                .andExpect(jsonPath("$.description").value(testPortfolio.getDescription()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreatePortfolio_UserNotFound() throws Exception {
        PortfolioCreateRequest request = new PortfolioCreateRequest();
        request.setName("Test Portfolio");
        request.setDescription("Test Description");

        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetPortfoliosForCurrentUser_Success() throws Exception {
        when(userService.findUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(portfolioService.findByUser(testUser)).thenReturn(Arrays.asList(testPortfolio));

        mockMvc.perform(get("/api/portfolios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testPortfolio.getId()))
                .andExpect(jsonPath("$[0].name").value(testPortfolio.getName()))
                .andExpect(jsonPath("$[0].description").value(testPortfolio.getDescription()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetPortfolioById_Success() throws Exception {
        when(portfolioService.findById(1L)).thenReturn(Optional.of(testPortfolio));

        mockMvc.perform(get("/api/portfolios/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testPortfolio.getId()))
                .andExpect(jsonPath("$.name").value(testPortfolio.getName()))
                .andExpect(jsonPath("$.description").value(testPortfolio.getDescription()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetPortfolioById_NotFound() throws Exception {
        when(portfolioService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/portfolios/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Portfolio not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testAddTransaction_Success() throws Exception {
        StockTransactionRequest request = new StockTransactionRequest();
        request.setStockSymbol("AAPL");
        request.setQuantity(BigDecimal.TEN);
        request.setPricePerShare(BigDecimal.valueOf(150.00));
        request.setTransactionType(TransactionType.BUY);
        request.setTransactionDate(LocalDateTime.now());

        when(portfolioService.addTransaction(eq(1L), any(StockTransactionRequest.class))).thenReturn(testTransaction);

        mockMvc.perform(post("/api/portfolios/1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testTransaction.getId()))
                .andExpect(jsonPath("$.stock.symbol").value(testTransaction.getStock().getSymbol()))
                .andExpect(jsonPath("$.quantity").value(testTransaction.getQuantity()));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetStockQuantity_Success() throws Exception {
        when(portfolioService.getStockQuantity(1L, "AAPL")).thenReturn(BigDecimal.TEN);

        mockMvc.perform(get("/api/portfolios/1/stocks/AAPL/quantity")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("10"));
    }
}