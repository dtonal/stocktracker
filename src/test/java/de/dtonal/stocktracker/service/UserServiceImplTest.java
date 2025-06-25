package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("Max Mustermann", "max@example.com", "password");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testRegisterNewUser() {
        when(userRepository.existsByEmailIgnoreCase(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerNewUser(user);
        assertThat(registered.getPassword()).isEqualTo("hashedPassword");
        assertThat(registered.getRoles()).contains(Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterNewUserAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase(user.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> userService.registerNewUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert bereits");
    }

    @Test
    void testFindUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserById(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByUsername() {
        when(userRepository.findByName("Max Mustermann")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserByUsername("Max Mustermann");
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void testFindUserByEmail() {
        when(userRepository.findByEmailIgnoreCase("max@example.com")).thenReturn(Optional.of(user));
        Optional<User> result = userService.findUserByEmail("max@example.com");
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
    }

    @Test
    void testFindAllUsers() {
        List<User> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);
        List<User> result = userService.findAllUsers();
        assertThat(result).containsExactly(user);
    }

    @Test
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert nicht");
    }

    @Test
    void testUpdateUser() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User updated = userService.updateUser(user);
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserNotFound() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.updateUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert nicht");
    }

    @Test
    void testUpdateUserPassword() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User updated = userService.updateUserPassword(user, "newPassword");
        assertThat(updated.getPassword()).isEqualTo("hashedNewPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserPasswordNotFound() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.updateUserPassword(user, "pw"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert nicht");
    }

    @Test
    void testUpdateUserRoles() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        List<Role> roles = List.of(Role.ADMIN, Role.USER);
        User updated = userService.updateUserRoles(user, roles);
        assertThat(updated.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserRolesNotFound() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.updateUserRoles(user, List.of(Role.ADMIN)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert nicht");
    }

    @Test
    void testUpdateUserLastLogin() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User updated = userService.updateUserLastLogin(user);
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserCreatedAt() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User updated = userService.updateUserCreatedAt(user);
        assertThat(updated.getCreatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserCreatedAtNotFound() {
        user.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> userService.updateUserCreatedAt(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("existiert nicht");
    }
}
