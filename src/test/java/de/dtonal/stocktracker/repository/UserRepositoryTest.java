package de.dtonal.stocktracker.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;

@DataJpaTest
@Tag("integration")
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        entityManager.clear();
    }

    @Test
    public void testSaveUser() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmailIgnoreCase(user.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(user.getName());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(passwordEncoder.matches("password", found.get().getPassword())).isTrue();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(Role.USER);
    }

    @Test
    public void testFindByEmail() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmailIgnoreCase(user.getEmail());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(user.getName());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(passwordEncoder.matches("password", found.get().getPassword())).isTrue();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(Role.USER);
    }

    @Test
    public void testFindByEmailNotFound() {
        Optional<User> found = userRepository.findByEmailIgnoreCase("nonexistent@example.com");

        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindByEmailCaseInsensitive() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByEmailIgnoreCase("John.Doe@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(user.getName());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(passwordEncoder.matches("password", found.get().getPassword())).isTrue();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(Role.USER);
    }

    @Test
    public void testExistsByEmail() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCase(user.getEmail());
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmailCaseInsensitive() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        boolean exists = userRepository.existsByEmailIgnoreCase("John.Doe@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByEmailNotFound() {
        boolean exists = userRepository.existsByEmailIgnoreCase("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    public void testFindByName() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        Optional<User> found = userRepository.findByName(user.getName());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(user.getName());
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(passwordEncoder.matches("password", found.get().getPassword())).isTrue();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(Role.USER);
    }

    @Test
    public void testFindByNameNotFound() {
        Optional<User> found = userRepository.findByName("nonexistent");
        assertThat(found).isNotPresent();
    }

    @Test
    public void testExistsByName() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testExistsByNameNotFound() {
        boolean exists = userRepository.existsByName("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    public void testExistsByNameCaseInsensitive() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testCreationDate() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    public void testUpdateUser() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        user.setName("Jane Doe");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase(user.getEmail());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Jane Doe");
        assertThat(found.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(passwordEncoder.matches("password", found.get().getPassword())).isTrue();
        assertThat(found.get().getRoles()).containsExactlyInAnyOrder(Role.USER);
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isAfter(user.getCreatedAt());
        assertThat(found.get().getUpdatedAt()).isBefore(LocalDateTime.now());
    }

    @Test
    public void testDeleteUser() {
        User user = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        userRepository.delete(user);

        Optional<User> found = userRepository.findByEmailIgnoreCase(user.getEmail());
        assertThat(found).isNotPresent();
    }

    @Test
    public void testFindAll() {
        User user1 = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        User user2 = new User("Jane Doe", "jane.doe@example.com", passwordEncoder.encode("password"));
        userRepository.saveAll(List.of(user1, user2));
        entityManager.flush();
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName).containsExactlyInAnyOrder(user1.getName(), user2.getName());
        assertThat(users).extracting(User::getEmail).containsExactlyInAnyOrder(user1.getEmail(), user2.getEmail());
        assertThat(users).extracting(User::getRoles).allMatch(roles -> roles.contains(Role.USER));
    }

    @Test
    public void testFindAllEmpty() {
        List<User> users = userRepository.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    public void testFindAllWithRoles() {
        User user1 = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        user1.addRole(Role.USER);
        userRepository.save(user1);
        entityManager.flush();
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        assertThat(users).extracting(User::getName).containsExactly(user1.getName());
        assertThat(users).extracting(User::getEmail).containsExactly(user1.getEmail());
        assertThat(users).extracting(User::getRoles).allMatch(roles -> roles.contains(Role.USER));
    }

    @Test
    public void testFindAllWithRolesEmpty() {
        List<User> users = userRepository.findAll();
        assertThat(users).isEmpty();
    }

    @Test
    public void testCreateUserWithSameEmailThrowsDataIntegrityViolationException() {
        User user = new User("John_Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        User user2 = new User("John Doe", "john.doe@example.com", passwordEncoder.encode("password"));
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            entityManager.flush(); // This will trigger the DataIntegrityViolationException
        })
                .isInstanceOf(ConstraintViolationException.class);
    }
}
