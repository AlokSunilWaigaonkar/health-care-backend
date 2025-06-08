package com.example.UserManagement.request;

import lombok.Data;

@Data
public class DoctorUpdateRequest {
    private String firstName;
    private String lastName;
    private String specialization;
    private String phoneNumber;
    private String qualification;
    private String availabilityHours;
}
