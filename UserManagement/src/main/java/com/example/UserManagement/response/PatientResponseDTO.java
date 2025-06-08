package com.example.UserManagement.response;

import com.example.UserManagement.model.Users.Patient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private Date dateOfBirth;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private String contactNo;
    private String medicalHistory;

    // Nested DTO for doctor's basic info
    private List<DoctorBasicInfo> doctors;



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DoctorBasicInfo {
        private Long id;
        private String FirstName;
        private String LastName;
        private String specialization;
    }



    public PatientResponseDTO(Patient patient) {
        this.id = patient.getId();
        this.email = patient.getEmail();
        this.firstName = patient.getFirstName();
        this.lastName = patient.getLastName();
        this.age = patient.getAge();
        this.gender = patient.getGender();
        this.dateOfBirth = patient.getDateOfBirth();
        this.address = patient.getAddress();
        this.bloodGroup = patient.getBloodGroup();
        this.emergencyContact = patient.getEmergencyContact();
        this.contactNo = patient.getContactNo();
        this.medicalHistory = patient.getMedicalHistory();



        if (patient.getDoctors() != null) {
            this.doctors = patient.getDoctors().stream()
                    .map(doc -> new DoctorBasicInfo(doc.getId(),doc.getFirstName(),doc.getLastName(), doc.getSpecialization()))
                    .collect(Collectors.toList());
        }
    }
}
