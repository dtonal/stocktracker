package de.dtonal.stocktracker.service;

import de.dtonal.stocktracker.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplUnitTest {

    @Mock
    private TransactionService transactionService;
    @Mock
    private PortfolioCalculationService portfolioCalculationService;

    @InjectMocks
    private PortfolioServiceImpl portfolioService;

    private User owner;
    private User anotherUser;
    private User admin;
    private Portfolio portfolio;
    private Stock stock;

    @BeforeEach
    void setUp() {
        owner = new User("Owner", "owner@example.com", "password");
        owner.setId("owner-id");
        owner.setRoles(Set.of(Role.USER));
        
        anotherUser = new User("Another", "another@example.com", "password");
        anotherUser.setId("another-user-id");
        anotherUser.setRoles(Set.of(Role.USER));

        admin = new User("Admin", "admin@example.com", "password");
        admin.setId("admin-id");
        admin.setRoles(Set.of(Role.ADMIN, Role.USER));

        portfolio = new Portfolio("Test Portfolio", "Description", owner);
        portfolio.setId("p1");

        stock = new Stock("AAPL", "Apple Inc.", "NASDAQ", "USD");
        stock.setId("s1");
    }

    @Test
    void authorizePortfolioAccess_shouldSucceed_whenUserIsOwner() {
        assertDoesNotThrow(() -> portfolioService.authorizePortfolioAccess(portfolio, owner));
    }

    @Test
    void authorizePortfolioAccess_shouldSucceed_whenUserIsAdmin() {
        assertDoesNotThrow(() -> portfolioService.authorizePortfolioAccess(portfolio, admin));
    }

    @Test
    void authorizePortfolioAccess_shouldThrowException_whenUserIsNotOwnerAndNotAdmin() {
        assertThatThrownBy(() -> portfolioService.authorizePortfolioAccess(portfolio, anotherUser))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("You are not authorized to delete this portfolio.");
    }

    @Test
    void isUserOwnerOrAdmin_shouldReturnTrue_forOwner() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("owner@example.com");
        when(auth.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        boolean result = portfolioService.isUserOwnerOrAdmin(portfolio, auth);

        assertThat(result).isTrue();
    }

    @Test
    void isUserOwnerOrAdmin_shouldReturnTrue_forAdmin() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@example.com");
        when(auth.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        boolean result = portfolioService.isUserOwnerOrAdmin(portfolio, auth);

        assertThat(result).isTrue();
    }

    @Test
    void isUserOwnerOrAdmin_shouldReturnFalse_forOtherUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("another@example.com");
        when(auth.getAuthorities()).thenAnswer(invocation -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        boolean result = portfolioService.isUserOwnerOrAdmin(portfolio, auth);

        assertThat(result).isFalse();
    }
} 