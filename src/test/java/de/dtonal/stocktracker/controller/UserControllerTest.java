package de.dtonal.stocktracker.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.dtonal.stocktracker.config.ApplicationConfig;
import de.dtonal.stocktracker.config.SecurityConfig;
import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.UserServiceImpl;

@WebMvcTest(controllers = UserController.class)
@Import({ SecurityConfig.class, ApplicationConfig.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceImpl userService;

    @MockBean
    private JwtService jwtService;

    private User testUser;
    private UserRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setRoles(Set.of(Role.USER));

        validRequest = new UserRegistrationRequest("Test User", "test@example.com", "password");
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.registerNewUser(any())).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }

    @Test
    void testRegisterUser_DuplicateEmail() throws Exception {
        when(userService.registerNewUser(any())).thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }

    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        when(userService.findUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser
    void testGetUserById_InvalidId() throws Exception {
        when(userService.findUserById(-1L)).thenThrow(new IllegalArgumentException("Invalid user ID"));

        mockMvc.perform(get("/api/users/-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid user ID"));
    }
}