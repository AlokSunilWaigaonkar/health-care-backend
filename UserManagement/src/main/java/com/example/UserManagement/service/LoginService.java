package com.example.UserManagement.service;


import com.example.UserManagement.model.AuthResponse;
import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.config.JwtTokenUtil;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;

    public AuthResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email);

        // Try to find as doctor first
        Optional<Doctor> doctorOpt = doctorRepo.findBySystemEmail(email);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccessToken(accessToken);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setUserId(doctor.getId());
            authResponse.setRole(Role.DOCTOR);
            return authResponse;
        }


        // Try to find as user (patient)
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccessToken(accessToken);
            authResponse.setRefreshToken(refreshToken);
            authResponse.setUserId(user.getId());
            String authority = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("PATIENT");
            authResponse.setRole(Role.valueOf(authority));
            return authResponse;
        }

        // If neither doctor nor user found, throw a handled exception
        throw new UsernameNotFoundException("User or Doctor not found with email: " + email);
    }
}
