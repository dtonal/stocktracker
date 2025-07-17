package de.dtonal.stocktracker.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.dtonal.stocktracker.config.ApplicationConfig;
import de.dtonal.stocktracker.config.SecurityConfig;
import de.dtonal.stocktracker.dto.UserResponse;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.UserRepository;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.UserService;

@WebMvcTest(UserController.class)
@Import({ SecurityConfig.class, ApplicationConfig.class })
@Tag("integration")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setPassword("password");
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetUserById() throws Exception {
        when(userService.findUserById(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", "some-id"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new UserResponse(user))));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void testGetUserById_NotFound() throws Exception {
        when(userService.findUserById(anyString())).thenThrow(new AccessDeniedException("Access is denied"));

        mockMvc.perform(get("/api/users/{id}", "non-existent-id"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentUser_whenAuthenticated_shouldReturnUserData() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId("user-id-123");
        mockUser.setName("Test User");
        mockUser.setEmail("test@example.com");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .with(authentication(mockAuthentication))) // Simuliert einen authentifizierten Benutzer
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-id-123"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me")) // Anfrage ohne Authentifizierung
                .andExpect(status().isUnauthorized());
    }
}