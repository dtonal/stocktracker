package de.dtonal.stocktracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.dtonal.stocktracker.dto.AuthenticationRequest;
import de.dtonal.stocktracker.dto.AuthenticationResponse;
import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.dto.UserResponse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Tag("integration")
public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void registerLoginAndAccessProtectedEndpoint_shouldSucceed() throws Exception {
                // Step 1: Register a new user
                UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                                "Test User", "testuser@example.com", "password123");

                MvcResult registrationResult = mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registrationRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String registrationResponseContent = registrationResult.getResponse().getContentAsString();
                UserResponse registrationResponse = objectMapper.readValue(registrationResponseContent,
                                UserResponse.class);
                String userId = registrationResponse.getId();
                assertThat(userId).isNotNull();

                // Step 2: Login with the new user
                AuthenticationRequest loginRequest = new AuthenticationRequest(
                                "testuser@example.com", "password123");

                MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn();

                String loginResponseContent = loginResult.getResponse().getContentAsString();
                AuthenticationResponse loginResponse = objectMapper.readValue(loginResponseContent,
                                AuthenticationResponse.class);
                String jwtToken = loginResponse.getToken();
                assertThat(jwtToken).isNotNull().isNotBlank();

                // Step 3: Access a protected endpoint with the JWT token
                mockMvc.perform(get("/api/users/" + userId)
                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(userId))
                                .andExpect(jsonPath("$.email").value("testuser@example.com"));
        }

        @Test
        void accessProtectedEndpointWithInvalidToken_shouldFail() throws Exception {
                // Step 1: Register a new user
                UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                                "Test User 2", "testuser2@example.com", "password123");

                MvcResult registrationResult = mockMvc.perform(post("/api/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registrationRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String registrationResponseContent = registrationResult.getResponse().getContentAsString();
                UserResponse registrationResponse = objectMapper.readValue(registrationResponseContent, UserResponse.class);
                String userId = registrationResponse.getId();
                assertThat(userId).isNotNull();

                // Step 2 & 3: Attempt to access a protected endpoint with an invalid JWT token
                mockMvc.perform(get("/api/users/" + userId)
                                .header("Authorization", "Bearer " + "invalid-jwt-token"))
                                .andExpect(status().isForbidden());
        }
}