package de.dtonal.stocktracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerNewUser(User user) {
        // Prüfe, ob Benutzer bereits existiert
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Benutzer mit dieser E-Mail-Adresse existiert bereits");
        }

        // Verschlüssele das Passwort
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Setze Standardrolle USER, falls keine Rolle gesetzt ist
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.addRole(Role.USER);
        }

        // Setze Timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByName(username);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Benutzer mit ID " + id + " existiert nicht");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Benutzer mit ID " + user.getId() + " existiert nicht");
        }

        // Aktualisiere den updatedAt Timestamp
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUserPassword(User user, String newPassword) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Benutzer mit ID " + user.getId() + " existiert nicht");
        }

        // Verschlüssele das neue Passwort
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUserRoles(User user, List<Role> roles) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Benutzer mit ID " + user.getId() + " existiert nicht");
        }

        // Lösche alle bestehenden Rollen und setze die neuen
        user.getRoles().clear();
        for (Role role : roles) {
            user.addRole(role);
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUserLastLogin(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Benutzer mit ID " + user.getId() + " existiert nicht");
        }

        // Setze lastLogin (falls das Feld existiert) und updatedAt
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Override
    public User updateUserCreatedAt(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Benutzer mit ID " + user.getId() + " existiert nicht");
        }

        // Setze createdAt auf aktuelle Zeit
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
