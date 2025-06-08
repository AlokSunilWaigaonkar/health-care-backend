package com.example.UserManagement.service;

import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.model.VerificationToken;
import com.example.UserManagement.request.RegisterRequest;
import com.example.UserManagement.response.UserResponseDTO;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<UserResponseDTO> getUsers();
    User registerUser(RegisterRequest request);
    Optional<User> findByEmail(String email);
    String validateToken(String token);
    VerificationToken generateNewVerificationToken(String oldToken);
}
