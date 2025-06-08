package com.example.UserManagement.model.AdditionalModel;

import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.model.Users.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.mapping.Join;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class NotificationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id",nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id" , nullable = false)
    private Patient patient;

    private String message;
    private LocalDateTime createdAt;

}
