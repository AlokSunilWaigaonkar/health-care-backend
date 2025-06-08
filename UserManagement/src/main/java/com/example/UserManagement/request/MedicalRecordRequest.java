package com.example.UserManagement.request;

import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.model.Users.User;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.Date;

@Data
public class MedicalRecordRequest {
    private Patient patient ;
    private String diagnosis;
    private String treatment;
    private Date dateOfVisit;
}
