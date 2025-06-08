package com.example.UserManagement.request;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private long doctorCount;
    private long patientCount;
    private long appointmentCount;

    public AdminStatsDTO(long doctorCount, long patientCount, long appointmentCount) {
        this.doctorCount = doctorCount;
        this.patientCount = patientCount;
        this.appointmentCount = appointmentCount;
    }
}
