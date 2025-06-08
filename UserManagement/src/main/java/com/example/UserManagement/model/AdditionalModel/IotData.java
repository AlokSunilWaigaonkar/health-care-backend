package com.example.UserManagement.model.AdditionalModel;

import com.example.UserManagement.model.Users.Patient;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class IotData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;
    private String field1; // Room Temp
    private String field2; // Humidity
    private String field3; // Body Temp
    private String field4; // SpO2
    private String field5; // ECG
    private LocalDateTime createdAt; // Timestamp for the data entry
}
