package de.dtonal.stocktracker.controller;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.dto.UserResponse;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        
        try {
            User userToRegister = new User();
            userToRegister.setName(request.getName());
            userToRegister.setEmail(request.getEmail());
            userToRegister.setPassword(request.getPassword());
            userToRegister.setRoles(Set.of(Role.USER));
            userToRegister.setCreatedAt(LocalDateTime.now());
            userToRegister.setUpdatedAt(LocalDateTime.now());
            User registeredUser = userService.registerNewUser(userToRegister);
            return ResponseEntity.ok(new UserResponse(registeredUser));
        } catch (IllegalArgumentException ex) {
            // Fehlerhafte Anfrage, z.B. Benutzer existiert bereits
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        UserResponse response = userService.findUserById(id).map(user -> new UserResponse(user))
        .orElse(null);
        return ResponseEntity.ok(response);
    }

}
