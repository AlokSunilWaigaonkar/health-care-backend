package com.example.UserManagement.response;

import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.Enums.AppointmentStatus;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    private Long appointmentId;
    private LocalDateTime appointmentDate;
    private String reason;
    private AppointmentStatus status;
    private DoctorInfo doctor;
    private PatientInfo patient;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String specialization;

        public DoctorInfo(String s, String specialization) {
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private int age;
        private String gender;

        public PatientInfo(String firstName, String lastName) {
        }
    }

    public AppointmentResponseDTO(Appointment appointment) {
        this.appointmentId = appointment.getId();
        this.appointmentDate = appointment.getAppointmentDate();
        this.reason = appointment.getReason();
        this.status = appointment.getStatus(); // Make sure status is String or convert accordingly

        Doctor doc = appointment.getDoctor();
        this.doctor = new DoctorInfo(doc.getId(), doc.getFirstName(), doc.getLastName(), doc.getSpecialization());

        Patient pat = appointment.getPatient();
        this.patient = new PatientInfo(pat.getId(), pat.getFirstName(), pat.getLastName(), pat.getAge(), pat.getGender());
    }

}
