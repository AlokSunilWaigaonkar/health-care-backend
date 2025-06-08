package com.example.UserManagement.response;

import com.example.UserManagement.model.Users.Doctor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String licenseNumber;
    private String qualification;
    private String phoneNumber;
    private String gender;
    private String availabilityHours;

    public DoctorResponseDTO(Doctor doctor) {
        this.id = doctor.getId();
        this.firstName= doctor.getFirstName();
        this.lastName = doctor.getLastName();
        this.email = doctor.getEmail();
        this.specialization = doctor.getSpecialization();
        this.licenseNumber = doctor.getLicenseNumber();
        this.qualification = doctor.getQualification();
        this.phoneNumber = doctor.getPhoneNumber();
        this.gender = doctor.getGender();
        this.availabilityHours = doctor.getAvailabilityHours();
    }
}
