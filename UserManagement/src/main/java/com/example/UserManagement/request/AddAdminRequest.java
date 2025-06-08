package com.example.UserManagement.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
@Data
public class AddAdminRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    private String FirstName;
    private String LastName;
    @NotBlank(message = "Password is required")
    private String password;
    @NotNull(message = "Access level is required")
    private String accessLevel;
}
