package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Tag("integration")
class UserServiceImplTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Max Mustermann", "max@example.com", "password");
        user.setId("1L");
        user.setRoles(Set.of(Role.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testRegisterNewUser() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User userToRegister = new User("New User", "new@example.com", "new_password");
        User registered = userService.registerNewUser(userToRegister);

        assertThat(registered).isNotNull();
        assertThat(passwordEncoder.matches("new_password", registered.getPassword())).isTrue();
        assertThat(registered.getRoles()).contains(Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterNewUserAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.registerNewUser(user))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @WithMockUser(username = "max@example.com")
    void testFindUserById_SuccessAsOwner() {
        when(userRepository.findById("1L")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserById("1L");
        assertThat(result).isPresent().contains(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFindUserById_SuccessAsAdmin() {
        when(userRepository.findById("1L")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserById("1L");
        assertThat(result).isPresent().contains(user);
    }

    @Test
    @WithMockUser(username = "someone.else@example.com")
    void testFindUserById_FailsAsOtherUser() {
        when(userRepository.findById("1L")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.findUserById("1L"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFindUserById_NotFound() {
        when(userRepository.findById("1L")).thenReturn(Optional.empty());
        Optional<User> result = userService.findUserById("1L");
        assertThat(result).isEmpty();
    }

    // Other tests would follow a similar pattern, ensuring proper authentication
    // context
    // and mocking for each specific case. The following are simplified for brevity.

    @Test
    @WithMockUser
    void testFindByEmail() {
        when(userRepository.findByEmailIgnoreCase("max@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findByEmail("max@example.com");
        assertThat(result).isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFindAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> result = userService.findAllUsers();
        assertThat(result).containsExactly(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() {
        when(userRepository.existsById("1L")).thenReturn(true);
        doNothing().when(userRepository).deleteById("1L");
        userService.deleteUser("1L");
        verify(userRepository).deleteById("1L");
    }
}
