package de.dtonal.stocktracker.service;

import java.util.List;
import java.util.Optional;

import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;

public interface UserService {
    User registerNewUser(User user);
    Optional<User> findUserById(Long id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    List<User> findAllUsers();
    void deleteUser(Long id);
    User updateUser(User user);
    User updateUserPassword(User user, String newPassword);
    User updateUserRoles(User user, List<Role> roles);
    User updateUserLastLogin(User user);
    User updateUserCreatedAt(User user);
}
