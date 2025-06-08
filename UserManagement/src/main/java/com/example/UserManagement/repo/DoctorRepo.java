package com.example.UserManagement.repo;

import com.example.UserManagement.model.Users.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor,Long> {
    Doctor findByEmail(String email);

    Optional<Doctor> findBySystemEmail(String systemEmail);}
