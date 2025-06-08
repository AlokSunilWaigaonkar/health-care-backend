package com.example.UserManagement.repo;

import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Enums.Status;
import com.example.UserManagement.model.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatus(Role role, Status status);
}
