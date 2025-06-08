package com.example.UserManagement.service;

import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Enums.Status;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AppStartupRunner implements CommandLineRunner {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder; // Make sure to configure BCryptPasswordEncoder bean

    @Override
    public void run(String... args) {
        String defaultAdminEmail = "waigaonkaralok@gmail.com";
        String defaultAdminPassword = "a123";

        if (!userRepo.findByEmail(defaultAdminEmail).isPresent()) {
            User admin = new User();
            admin.setEmail(defaultAdminEmail);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setStatus(Status.APPROVED);
            userRepo.save(admin);
            System.out.println("✅ Default admin created.");
        } else {
            System.out.println("ℹ️ Admin already exists.");
        }
    }
}