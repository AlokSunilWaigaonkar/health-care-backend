package com.example.UserManagement.model.AdditionalModel;

import com.example.UserManagement.model.Enums.AppointmentStatus;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "patient_id" , nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id" , nullable = false)
    private Doctor doctor;

    private LocalDateTime appointmentDate;
    private String Reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

}
