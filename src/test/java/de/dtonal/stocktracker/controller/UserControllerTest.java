package de.dtonal.stocktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import de.dtonal.stocktracker.config.SecurityConfig;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("Max Mustermann", "max@example.com", "password123");
        testUser.setId(1L);
        testUser.setRoles(Set.of(Role.USER));
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        registrationRequest = new UserRegistrationRequest("Max Mustermann", "max@example.com", "password123");
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.registerNewUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Max Mustermann"))
                .andExpect(jsonPath("$.email").value("max@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        verify(userService).registerNewUser(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() throws Exception {
        when(userService.registerNewUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Benutzer existiert bereits"));

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());

        verify(userService).registerNewUser(any(User.class));
    }

    @Test
    void testRegisterUser_InvalidInput() throws Exception {
        UserRegistrationRequest invalidRequest = new UserRegistrationRequest("", "invalid-email", "short");

        mockMvc.perform(post("/api/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).registerNewUser(any(User.class));
    }

    @Test
    @WithMockUser
    void testGetUserById_Success() throws Exception {
        when(userService.findUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).findUserById(1L);
    }

    @Test
    @WithMockUser
    void testGetUserById_NotFound() throws Exception {
        when(userService.findUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userService).findUserById(999L);
    }

    @Test
    @WithMockUser
    void testGetUserById_InvalidPathVariable() throws Exception {
        mockMvc.perform(get("/api/users/invalid"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findUserById(any(Long.class));
    }
} 