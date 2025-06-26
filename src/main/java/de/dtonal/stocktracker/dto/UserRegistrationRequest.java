package de.dtonal.stocktracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationRequest {
    @NotBlank
    @Size(min = 3, max = 50, message = "Name muss zwischen 3 und 50 Zeichen lang sein")
    private String name;

    @NotBlank
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank
    @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
    private String password;

    // empty constructor
    public UserRegistrationRequest() {
    }

    public UserRegistrationRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }   

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'}";
    }
}
