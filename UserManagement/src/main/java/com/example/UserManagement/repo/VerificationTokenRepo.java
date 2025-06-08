package com.example.UserManagement.repo;

import com.example.UserManagement.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepo extends JpaRepository<VerificationToken,Long> {
    VerificationToken findByToken(String token);

    VerificationToken findByUserId(Long id);
}
