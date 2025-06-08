package com.example.UserManagement.config;

import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationDetailsService implements UserDetailsService {
    private final UserRepo userRepository;
    private final DoctorRepo doctorRepository;


    @Override
    public UserDetails loadUserByUsername(String emailOrSystemEmail) throws UsernameNotFoundException {

        System.out.println("Attempting login for: " + emailOrSystemEmail);

        // 1. Try Doctor login using systemEmail
        Doctor doctor = doctorRepository.findBySystemEmail(emailOrSystemEmail)
                .orElse(null);

        if (doctor != null) {
            return new DoctorUserDetails(doctor); // return DoctorUserDetails
        }

        // 2. Try User login using normal email
        User user = userRepository.findByEmail(emailOrSystemEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User/Doctor not found with email: " + emailOrSystemEmail));

        return new UserRegistrationDetails(user); // return UserRegistrationDetails

    }
}
