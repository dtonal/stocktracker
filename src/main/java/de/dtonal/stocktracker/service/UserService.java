package de.dtonal.stocktracker.service;

import java.util.List;
import java.util.Optional;

import de.dtonal.stocktracker.model.User;

public interface UserService {
    User registerNewUser(User user);

    Optional<User> findUserById(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findUserByUsername(String username);

    List<User> findAllUsers();

    void deleteUser(String id);

    User updateUser(User user);
}
