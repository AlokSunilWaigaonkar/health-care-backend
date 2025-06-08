package com.example.UserManagement.service;

import com.example.UserManagement.model.RefreshToken;
import com.example.UserManagement.repo.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveToken(String email, String token) {
        refreshTokenRepository.deleteByEmail(email); // Remove old token if any
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(token);
        refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenValid(String email, String token) {
        return refreshTokenRepository.findByEmail(email)
                .map(stored -> stored.getToken().equals(token))
                .orElse(false);
    }

    public void deleteToken(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }
}