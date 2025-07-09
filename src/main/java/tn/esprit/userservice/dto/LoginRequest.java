package tn.esprit.userservice.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier; // peut être email ou username
    private String password;

    // getters & setters
}
