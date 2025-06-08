package com.example.UserManagement.model.Users;

import com.example.UserManagement.model.Enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String specialization;
    private String licenseNumber;
    private String email;
    private String qualification;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private String availabilityHours;
    private String systemEmail; // System-generated email
    private String systemPassword;
    @Immutable
    private Role role = Role.DOCTOR;
    // Hashed password

    @ManyToMany(mappedBy = "doctors")
    @JsonIgnore
    private List<Patient> patients= new ArrayList<>(); // One doctor can have many patients


    
}
