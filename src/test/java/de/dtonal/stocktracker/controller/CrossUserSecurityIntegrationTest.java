package de.dtonal.stocktracker.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import de.dtonal.stocktracker.dto.StockTransactionRequest;
import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.Stock;
import de.dtonal.stocktracker.model.TransactionType;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.PortfolioRepository;
import de.dtonal.stocktracker.repository.StockRepository;
import de.dtonal.stocktracker.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CrossUserSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private Portfolio portfolioOfUserB;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        // Clean up database
        userRepository.deleteAll();
        portfolioRepository.deleteAll();
        stockRepository.deleteAll();

        // Create User A (the attacker)
        userA = new User("User A", "userA@example.com", passwordEncoder.encode("passwordA"));
        userRepository.save(userA);

        // Create User B (the victim)
        userB = new User("User B", "userB@example.com", passwordEncoder.encode("passwordB"));
        userRepository.save(userB);

        // Create a portfolio for User B
        portfolioOfUserB = new Portfolio("User B's Portfolio", "", userB);
        portfolioRepository.save(portfolioOfUserB);
        
        // Create a stock for transactions
        testStock = new Stock("SECURE", "Security Test Stock", "SEC", "USD");
        stockRepository.save(testStock);
    }

    private String authenticateAndGetToken(String email, String password) throws Exception {
        AuthenticationRequest authRequest = new AuthenticationRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void testGetOtherUsersPortfolioFails() throws Exception {
        // Step 1: User A authenticates
        String tokenOfUserA = authenticateAndGetToken("userA@example.com", "passwordA");

        // Step 2: User A tries to access User B's portfolio
        mockMvc.perform(get("/api/portfolios/" + portfolioOfUserB.getId())
                .header("Authorization", "Bearer " + tokenOfUserA))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddTransactionToOtherUsersPortfolioFails() throws Exception {
        // Step 1: User A authenticates
        String tokenOfUserA = authenticateAndGetToken("userA@example.com", "passwordA");

        // Step 2: User A prepares a transaction for User B's portfolio
        StockTransactionRequest transactionRequest = new StockTransactionRequest(
            portfolioOfUserB.getId(),
            testStock.getId(),
            LocalDateTime.now(),
            new BigDecimal("100"),
            new BigDecimal("1"),
            TransactionType.BUY,
            testStock.getSymbol()
        );

        // Step 3: User A tries to add the transaction to User B's portfolio
        mockMvc.perform(post("/api/portfolios/" + portfolioOfUserB.getId() + "/transactions")
                .header("Authorization", "Bearer " + tokenOfUserA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isForbidden());
    }
} 