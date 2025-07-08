package de.dtonal.stocktracker.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.dtonal.stocktracker.config.ApplicationConfig;
import de.dtonal.stocktracker.config.SecurityConfig;
import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.PortfolioUpdateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.PortfolioNotFoundException;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.StockTransaction;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.repository.UserRepository;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.PortfolioService;

@WebMvcTest(PortfolioController.class)
@Import({ ApplicationConfig.class, SecurityConfig.class })
@Tag("integration")
public class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void createPortfolio_shouldSucceed() throws Exception {
        PortfolioCreateRequest request = new PortfolioCreateRequest("My Portfolio", "A test portfolio");
        Portfolio portfolio = new Portfolio();
        portfolio.setName("My Portfolio");
        portfolio.setId("uuid-123");
        // User setzen, falls PortfolioResponse darauf zugreift
        de.dtonal.stocktracker.model.User user = new de.dtonal.stocktracker.model.User();
        user.setEmail("test@example.com");
        portfolio.setUser(user);

        when(portfolioService.createPortfolio(any(PortfolioCreateRequest.class))).thenReturn(portfolio);

        mockMvc.perform(post("/api/portfolios")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("uuid-123"))
                .andExpect(jsonPath("$.name").value("My Portfolio"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void getPortfoliosForCurrentUser_shouldSucceed() throws Exception {
        Portfolio portfolio1 = new Portfolio();
        portfolio1.setId("uuid-1");
        de.dtonal.stocktracker.model.User user1 = new de.dtonal.stocktracker.model.User();
        user1.setEmail("test@example.com");
        portfolio1.setUser(user1);

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setId("uuid-2");
        de.dtonal.stocktracker.model.User user2 = new de.dtonal.stocktracker.model.User();
        user2.setEmail("test2@example.com");
        portfolio2.setUser(user2);

        when(portfolioService.findPortfoliosForCurrentUser()).thenReturn(List.of(portfolio1, portfolio2));

        mockMvc.perform(get("/api/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void addStockToPortfolio_shouldSucceed() throws Exception {
        StockTransactionRequest request = new StockTransactionRequest("portfolio-123", "stock-123", LocalDateTime.now(),
                BigDecimal.valueOf(10), BigDecimal.valueOf(150.0), TransactionType.BUY, "AAPL");
        StockTransaction transaction = new StockTransaction();
        transaction.setId("tx-uuid-456");
        // Falls im Response auf weitere Felder zugegriffen wird:
        Portfolio portfolio = new Portfolio();
        portfolio.setId("uuid-123");

        de.dtonal.stocktracker.model.User user = new de.dtonal.stocktracker.model.User();
        user.setEmail("test@example.com");
        portfolio.setUser(user);

        transaction.setPortfolio(portfolio);

        Stock stock = new Stock();
        stock.setId("stock-123");
        stock.setSymbol("AAPL");
        transaction.setStock(stock);

        when(portfolioService.addStockTransaction(anyString(), any(StockTransactionRequest.class)))
                .thenReturn(transaction);

        mockMvc.perform(post("/api/portfolios/{portfolioId}/transactions", "uuid-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("tx-uuid-456"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deletePortfolio_shouldSucceed() throws Exception {
        doNothing().when(portfolioService).deletePortfolio("portfolio-123");

        mockMvc.perform(delete("/api/portfolios/{id}", "portfolio-123")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deletePortfolio_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new PortfolioNotFoundException("Portfolio not found"))
                .when(portfolioService).deletePortfolio("non-existent-id");

        mockMvc.perform(delete("/api/portfolios/{id}", "non-existent-id")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Portfolio not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deletePortfolio_whenAccessDenied_shouldReturn403() throws Exception {
        doThrow(new AccessDeniedException("Access denied"))
                .when(portfolioService).deletePortfolio("other-user-portfolio-id");

        mockMvc.perform(delete("/api/portfolios/{id}", "other-user-portfolio-id")
                .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updatePortfolio_shouldSucceed() throws Exception {
        PortfolioUpdateRequest updateRequest = new PortfolioUpdateRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        Portfolio updatedPortfolio = new Portfolio();
        updatedPortfolio.setId("portfolio-123");
        updatedPortfolio.setName("Updated Name");
        de.dtonal.stocktracker.model.User user = new de.dtonal.stocktracker.model.User();
        user.setEmail("test@example.com");
        updatedPortfolio.setUser(user);

        when(portfolioService.updatePortfolio(anyString(), any(PortfolioUpdateRequest.class))).thenReturn(updatedPortfolio);

        mockMvc.perform(put("/api/portfolios/{id}", "portfolio-123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updatePortfolio_whenNotFound_shouldReturn404() throws Exception {
        PortfolioUpdateRequest updateRequest = new PortfolioUpdateRequest();
        updateRequest.setName("Updated Name");

        when(portfolioService.updatePortfolio(anyString(), any(PortfolioUpdateRequest.class)))
                .thenThrow(new PortfolioNotFoundException("Portfolio not found"));

        mockMvc.perform(put("/api/portfolios/{id}", "non-existent-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteStockTransaction_shouldSucceed() throws Exception {
        doNothing().when(portfolioService).deleteStockTransaction("portfolio-123", "tx-456");

        mockMvc.perform(delete("/api/portfolios/{portfolioId}/transactions/{transactionId}", "portfolio-123", "tx-456")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteStockTransaction_whenPortfolioNotFound_shouldReturn404() throws Exception {
        doThrow(new PortfolioNotFoundException("Portfolio not found"))
                .when(portfolioService).deleteStockTransaction("non-existent-portfolio", "tx-456");

        mockMvc.perform(delete("/api/portfolios/{portfolioId}/transactions/{transactionId}", "non-existent-portfolio", "tx-456")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
