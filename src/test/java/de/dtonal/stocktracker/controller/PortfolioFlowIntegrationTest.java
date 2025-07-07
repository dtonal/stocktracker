package de.dtonal.stocktracker.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import de.dtonal.stocktracker.dto.AuthenticationRequest;
import de.dtonal.stocktracker.dto.PortfolioCreateRequest;
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PortfolioFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        stockRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("flow-user@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Flow User");
        userRepository.save(testUser);

        testStock = new Stock("MSFT", "Microsoft Corp.", "NASDAQ", "USD");
        stockRepository.save(testStock);
    }

    private String authenticateAndGetToken() throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest("flow-user@example.com", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void testFullPortfolioAndTransactionFlow() throws Exception {
        // Step 1: Authenticate and get JWT
        String token = authenticateAndGetToken();

        // Step 2: Create a new portfolio
        PortfolioCreateRequest createRequest = new PortfolioCreateRequest("My Tech Portfolio", "A portfolio for tech stocks");
        
        MvcResult createPortfolioResult = mockMvc.perform(post("/api/portfolios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Tech Portfolio"))
                .andReturn();
        
        String portfolioId = JsonPath.read(createPortfolioResult.getResponse().getContentAsString(), "$.id");

        // Step 3: Add a stock transaction to the portfolio
        StockTransactionRequest transactionRequest = new StockTransactionRequest(
            portfolioId,
            testStock.getId(),
            LocalDateTime.now(),
            new BigDecimal("10"),
            new BigDecimal("300.50"),
            TransactionType.BUY,
            testStock.getSymbol()
        );

        mockMvc.perform(post("/api/portfolios/" + portfolioId + "/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stockSymbol").value("MSFT"))
                .andExpect(jsonPath("$.quantity").value(10));

        // Step 4: Verify the state by fetching the portfolio
        mockMvc.perform(get("/api/portfolios/" + portfolioId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Tech Portfolio"))
                .andExpect(jsonPath("$.transactions[0].stockSymbol").value("MSFT"))
                .andExpect(jsonPath("$.transactions[0].quantity").value(10))
                .andExpect(jsonPath("$.transactions[0].pricePerShare").value(300.50));
    }

    @Test
    void testPortfolioDeleteFlow_Success() throws Exception {
        // Step 1: Authenticate and get JWT
        String token = authenticateAndGetToken();

        // Step 2: Create a portfolio to be deleted
        PortfolioCreateRequest createRequest = new PortfolioCreateRequest("To Be Deleted", "This portfolio will be deleted.");
        MvcResult createResult = mockMvc.perform(post("/api/portfolios")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        String portfolioId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        // Step 3: Delete the portfolio
        mockMvc.perform(delete("/api/portfolios/{id}", portfolioId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Step 4: Verify deletion by trying to fetch it again (should result in 404)
        mockMvc.perform(get("/api/portfolios/{id}", portfolioId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPortfolioDeleteFlow_Forbidden() throws Exception {
        // Step 1: Create a user and a portfolio for them
        String ownerToken = authenticateAndGetToken();
        PortfolioCreateRequest createRequest = new PortfolioCreateRequest("Owner's Portfolio", "This belongs to the owner.");
        MvcResult createResult = mockMvc.perform(post("/api/portfolios")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        String portfolioId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        // Step 2: Create a second user and try to delete the first user's portfolio
        User attackerUser = new User();
        attackerUser.setEmail("attacker@example.com");
        attackerUser.setPassword(passwordEncoder.encode("password123"));
        attackerUser.setName("Attacker User");
        userRepository.save(attackerUser);

        AuthenticationRequest authRequest = new AuthenticationRequest("attacker@example.com", "password123");
        MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String attackerToken = JsonPath.read(authResult.getResponse().getContentAsString(), "$.token");

        // Step 3: Attacker tries to delete the portfolio
        mockMvc.perform(delete("/api/portfolios/{id}", portfolioId)
                .header("Authorization", "Bearer " + attackerToken))
                .andExpect(status().isForbidden());
    }
} 