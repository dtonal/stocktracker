package de.dtonal.stocktracker.controller;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.dtonal.stocktracker.dto.AuthenticationResponse;
import de.dtonal.stocktracker.dto.UserRegistrationRequest;
import de.dtonal.stocktracker.dto.UserResponse;
import de.dtonal.stocktracker.model.Role;
import de.dtonal.stocktracker.model.User;
import de.dtonal.stocktracker.service.JwtService;
import de.dtonal.stocktracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    
    @Autowired
    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
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
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        try {
            return userService.findUserById(id)
                    .map(user -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new UserResponse(user)))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
