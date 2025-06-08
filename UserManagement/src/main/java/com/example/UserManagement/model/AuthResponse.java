package com.example.UserManagement.model;

import com.example.UserManagement.model.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long UserId;
    private String accessToken;
    private String refreshToken;
    private Role role;


}
