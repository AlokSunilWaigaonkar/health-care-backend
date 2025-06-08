package com.example.UserManagement.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DoctorRegisterRequest {
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

}

