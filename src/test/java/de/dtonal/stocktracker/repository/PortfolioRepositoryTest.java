package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.dtonal.stocktracker.model.Portfolio;
import de.dtonal.stocktracker.model.User;

@DataJpaTest
public class PortfolioRepositoryTest {
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        entityManager.clear();

        // Erstelle einen Testbenutzer
        testUser = new User("Test User", "test@example.com", passwordEncoder.encode("password"));
        userRepository.save(testUser);
        entityManager.flush();
    }

    @Test
    public void testSavePortfolio() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        Optional<Portfolio> found = portfolioRepository.findById(portfolio.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Portfolio");
        assertThat(found.get().getDescription()).isEqualTo("Test Description");
        assertThat(found.get().getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testFindByUser() {
        Portfolio portfolio1 = new Portfolio("Portfolio 1", "Description 1", testUser);
        Portfolio portfolio2 = new Portfolio("Portfolio 2", "Description 2", testUser);
        portfolioRepository.saveAll(List.of(portfolio1, portfolio2));
        entityManager.flush();
        entityManager.clear();

        List<Portfolio> found = portfolioRepository.findByUser(testUser);
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Portfolio::getName).containsExactlyInAnyOrder("Portfolio 1", "Portfolio 2");
        assertThat(found).extracting(Portfolio::getUser).allMatch(user -> user.getId() == testUser.getId());
    }

    @Test
    public void testFindByUserId() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        List<Portfolio> found = portfolioRepository.findByUserId(testUser.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Test Portfolio");
        assertThat(found.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    public void testFindByNameAndUser() {
        Portfolio portfolio = new Portfolio("Unique Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        Portfolio found = portfolioRepository.findByNameAndUser("Unique Portfolio", testUser);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Unique Portfolio");
        assertThat(found.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    public void testFindByNameAndUserNotFound() {
        Portfolio found = portfolioRepository.findByNameAndUser("Non Existent", testUser);
        assertThat(found).isNull();
    }

    @Test
    public void testExistsByNameAndUser() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        boolean exists = portfolioRepository.existsByNameAndUser("Test Portfolio", testUser);
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByNameAndUserNotFound() {
        boolean exists = portfolioRepository.existsByNameAndUser("Non Existent", testUser);
        assertThat(exists).isFalse();
    }

    @Test
    public void testUpdatePortfolio() {
        Portfolio portfolio = new Portfolio("Original Name", "Original Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        portfolio.setName("Updated Name");
        portfolio.setDescription("Updated Description");
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        Optional<Portfolio> found = portfolioRepository.findById(portfolio.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Updated Name");
        assertThat(found.get().getDescription()).isEqualTo("Updated Description");
        assertThat(found.get().getUpdatedAt()).isAfter(found.get().getCreatedAt());
    }

    @Test
    public void testDeletePortfolio() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        portfolioRepository.delete(portfolio);
        entityManager.flush();

        Optional<Portfolio> found = portfolioRepository.findById(portfolio.getId());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Portfolio portfolio1 = new Portfolio("Portfolio 1", "Description 1", testUser);
        Portfolio portfolio2 = new Portfolio("Portfolio 2", "Description 2", testUser);
        portfolioRepository.saveAll(List.of(portfolio1, portfolio2));
        entityManager.flush();
        entityManager.clear();

        List<Portfolio> all = portfolioRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Portfolio::getName).containsExactlyInAnyOrder("Portfolio 1", "Portfolio 2");
    }

    @Test
    public void testFindAllEmpty() {
        List<Portfolio> all = portfolioRepository.findAll();
        assertThat(all).isEmpty();
    }

    @Test
    public void testCreationDate() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        Optional<Portfolio> found = portfolioRepository.findById(portfolio.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getCreatedAt()).isCloseTo(found.get().getUpdatedAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    public void testUpdateDate() {
        Portfolio portfolio = new Portfolio("Test Portfolio", "Test Description", testUser);
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        LocalDateTime originalUpdatedAt = portfolio.getUpdatedAt();

        portfolio.setName("Updated Name");
        portfolioRepository.save(portfolio);
        entityManager.flush();
        entityManager.clear();

        Optional<Portfolio> found = portfolioRepository.findById(portfolio.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    public void testFindByUserWithMultipleUsers() {
        User user2 = new User("User 2", "user2@example.com", passwordEncoder.encode("password"));
        userRepository.save(user2);

        Portfolio portfolio1 = new Portfolio("Portfolio 1", "Description 1", testUser);
        Portfolio portfolio2 = new Portfolio("Portfolio 2", "Description 2", user2);
        portfolioRepository.saveAll(List.of(portfolio1, portfolio2));
        entityManager.flush();
        entityManager.clear();

        List<Portfolio> user1Portfolios = portfolioRepository.findByUser(testUser);
        List<Portfolio> user2Portfolios = portfolioRepository.findByUser(user2);

        assertThat(user1Portfolios).hasSize(1);
        assertThat(user2Portfolios).hasSize(1);
        assertThat(user1Portfolios.get(0).getUser().getId()).isEqualTo(testUser.getId());
        assertThat(user2Portfolios.get(0).getUser().getId()).isEqualTo(user2.getId());
    }

    @Test
    public void testFindByUserIdNotFound() {
        List<Portfolio> found = portfolioRepository.findByUserId("999L");
        assertThat(found).isEmpty();
    }
}