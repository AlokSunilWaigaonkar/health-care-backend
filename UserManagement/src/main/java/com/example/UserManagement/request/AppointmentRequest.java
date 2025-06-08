package com.example.UserManagement.request;

import com.example.UserManagement.model.Enums.AppointmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private LocalDateTime appointmentDate;
    private String reason;
    private AppointmentStatus status = AppointmentStatus.PENDING;
}
