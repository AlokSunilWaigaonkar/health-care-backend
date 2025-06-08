package com.example.UserManagement.service;

import com.example.UserManagement.exception.UserAlreadyExistsException;
import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.model.VerificationToken;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.UserRepo;
import com.example.UserManagement.repo.VerificationTokenRepo;
import com.example.UserManagement.request.RegisterRequest;
import com.example.UserManagement.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepo userRepo;
    private final DoctorRepo doctorRepo;
    private final VerificationTokenRepo verificationTokenRepo;
    private final PasswordEncoder passwordEncoder;
     // Inject JavaMailSender for email functionality


    @Override
    public List<UserResponseDTO> getUsers() {
        List<User> users = userRepo.findAll();
        return users.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        user.isEnabled(),
                        user.getRole(),
                        user.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public User registerUser(RegisterRequest request) {

        Optional<User> user = userRepo.findByEmail(request.getEmail());
        if(user.isPresent()){
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + "already exists");
        }
        User newUser = new User();
        newUser.setEmail(request.getEmail());
        String role = request.getRole();
        Role userRole = (role != null && !role.isEmpty()) ? Role.valueOf(role) : Role.PATIENT;
        newUser.setRole(userRole);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepo.save(newUser);
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    @Override
    public String validateToken(String token) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(token);
        if (verificationToken == null){
            return "Invalid verification token";
        }
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if (verificationToken.getExpirationTime().getTime()-calendar.getTime().getTime()<=0){
            verificationTokenRepo.delete(verificationToken);
            return "Token already expired";
        }
        user.setEnabled(true);
        userRepo.save(user) ;
        return "Valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepo.findByToken(oldToken);

        if (verificationToken == null) {
            throw new IllegalArgumentException("Invalid verification token");
        }

        // Generate a new token string
        verificationToken.setToken(UUID.randomUUID().toString());

        // Set new expiration time (e.g., 10 minutes from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        verificationToken.setExpirationTime(calendar.getTime());

        return verificationTokenRepo.save(verificationToken);
    }



    public void saveUserVerification(User user, String token){
        VerificationToken verificationToken = new VerificationToken(user,token);
        verificationTokenRepo.save(verificationToken);
    }
}
