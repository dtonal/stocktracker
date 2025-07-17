package de.dtonal.stocktracker.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.dto.UserResponse;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            User userToRegister = new User();
            userToRegister.setName(request.getName());
            userToRegister.setEmail(request.getEmail());
            userToRegister.setPassword(request.getPassword());
            userToRegister.setRoles(Set.of(Role.USER));

            User registeredUser = userService.registerNewUser(userToRegister);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UserResponse(registeredUser));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return userService.findUserById(id)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            return ResponseEntity.ok(new UserResponse(user));
        }

        // Fallback, falls der Principal nicht vom erwarteten Typ ist (sollte nicht
        // passieren)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

}
