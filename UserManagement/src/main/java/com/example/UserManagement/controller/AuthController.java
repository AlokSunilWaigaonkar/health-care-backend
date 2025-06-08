package com.example.UserManagement.controller;

import com.example.UserManagement.config.JwtTokenUtil;
import com.example.UserManagement.config.UserRegistrationDetailsService;
import com.example.UserManagement.model.AuthResponse;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.model.VerificationToken;
import com.example.UserManagement.repo.VerificationTokenRepo;
import com.example.UserManagement.request.LoginRequest;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.service.LoginService;
import com.example.UserManagement.service.RefreshTokenService;
import com.example.UserManagement.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final UserService userService;
    private final VerificationTokenRepo verificationTokenRepo;
    private final RegistrationController registrationController;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRegistrationDetailsService userRegistrationDetailsService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request)
            throws MessagingException, UnsupportedEncodingException {
        try {
            AuthResponse response = loginService.login(loginRequest.getEmail(), loginRequest.getPassword());
            // response should have accessToken, refreshToken, and role
            return ResponseEntity.ok(new ApiResponseDTO<>("Login successful", true, response));
        } catch (DisabledException e) {
            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user != null && !user.isEnabled()) {
                VerificationToken oldToken = verificationTokenRepo.findByUserId(user.getId());
                registrationController.resendVerificationToken(oldToken.getToken(), request);
                System.out.println("Verification email re-sent: " + oldToken);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>("Account not verified. A new verification email has been sent.", false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Login failed due to an internal error: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO<Void>> logoutUser(HttpServletRequest request) {
        String token = extractJwtFromRequest(request);
        if (token != null) {
            String email = jwtTokenUtil.getUsernameFromToken(token);
            // Blacklist access token logic can be implemented if needed
            refreshTokenService.deleteToken(email);
            return ResponseEntity.ok(new ApiResponseDTO<>("User with email " + email + " logged out successfully", true, null));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponseDTO<>("No access token provided", false, null));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token missing or invalid"));
        }

        String refreshToken = authHeader.substring(7);

        if (!jwtTokenUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
        }

        String username = jwtTokenUtil.getUsernameFromToken(refreshToken);
        Optional<User> userDetails = userService.findByEmail(username);
        if (userDetails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }
        String newAccessToken = jwtTokenUtil.generateToken(userDetails.get().getEmail());
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails.get().getEmail());
        Map<String, String> tokens = new HashMap<>();
        tokens.put("token", newAccessToken);
        tokens.put("refreshToken", newRefreshToken); // optional if rotating

        return ResponseEntity.ok(tokens);
    }
}