package de.dtonal.stocktracker.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Tag("integration")
public class UserServiceImplSecuredTest {
    @Autowired
    private UserService userService; // Injiziert die ECHTE Service-Instanz

    @MockBean
    private UserRepository userRepository; // Das Repository mocken wir weiterhin

    @Test
    @WithMockUser(username = "another.user@example.com")
    void findUserById_whenUserIsNotAdminOrSelf_thenThrowsAccessDenied() {
        // Erstellen Sie einen User, der nicht dem MockUser entspricht
        User foundUser = new User("Test User", "test@example.com", "pw");
        when(userRepository.findById("1L")).thenReturn(Optional.of(foundUser));

        // Erwarten, dass die Sicherheitsregel eine Exception auslöst
        assertThrows(AccessDeniedException.class, () -> {
            userService.findUserById("1L");
        });
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void findUserById_whenUserIsSelf_thenAccessIsGranted() {
        User foundUser = new User("Test User", "test@example.com", "pw");
        when(userRepository.findById("1L")).thenReturn(Optional.of(foundUser));

        // Erwarten, dass KEINE Exception ausgelöst wird
        assertDoesNotThrow(() -> {
            userService.findUserById("1L");
        });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findUserById_whenUserIsAdmin_thenAccessIsGranted() {
        User foundUser = new User("Test User", "test@example.com", "pw");
        when(userRepository.findById("1L")).thenReturn(Optional.of(foundUser));

        assertDoesNotThrow(() -> {
            userService.findUserById("1L");
        });
    }
}
