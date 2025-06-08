package com.example.UserManagement.request;

import lombok.Data;

import java.util.Date;

@Data
public class PatientUpdateRequest {
    private String FirstName;
    private String LastName;
    private int age;
    private String gender;
    private String bloodGroup;
    private String emergencyContact;
    private Date DateOfBirth;
    private String Address;
    private String ContactNo;
    private String healthRecords;
    private String MedicalHistory;
}
