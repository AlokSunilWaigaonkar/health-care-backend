package com.example.UserManagement.response;

import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private boolean enabled;
    private Role role;
    private Status status;
}
