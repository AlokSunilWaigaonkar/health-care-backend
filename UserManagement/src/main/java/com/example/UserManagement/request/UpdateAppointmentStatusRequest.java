package com.example.UserManagement.request;

import com.example.UserManagement.model.Enums.AppointmentStatus;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {
    private Long appointmentId;
    private AppointmentStatus status;
}
