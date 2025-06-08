package com.example.UserManagement.controller;

import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.PatientRepo;
import com.example.UserManagement.request.AppointmentRequest;
import com.example.UserManagement.request.ChangePasswordRequest;
import com.example.UserManagement.request.PatientUpdateRequest;
import com.example.UserManagement.response.AppointmentResponseDTO;
import com.example.UserManagement.response.PatientResponseDTO;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final PatientRepo patientRepo;
    private final DoctorRepo doctorRepo;

    @PutMapping("/{patientId}/update")
    public ResponseEntity<ApiResponseDTO<Void>> updatePatientDetails(@PathVariable Long patientId,
                                                               @RequestBody PatientUpdateRequest patientUpdateRequest) {
        try {
            patientService.updatePatientDetails(patientId, patientUpdateRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>("Patient updated successfully", true, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>("Error: " + e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Unexpected error occurred while updating patient", false, null));
        }
    }

    @PostMapping("/book/{patientId}/{doctorId}")
    public ResponseEntity<ApiResponseDTO<Void>> bookAppointment(@PathVariable Long patientId,
                                                          @PathVariable Long doctorId,
                                                          @RequestBody AppointmentRequest appointmentRequest) {
        try {
            Patient patient = patientRepo.findById(patientId)
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(()->new RuntimeException("Doctor not found"));
            patientService.bookAppointment(doctor.getId(), patientId, appointmentRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>("Appointment booked successfully", true, null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(ex.getMessage(), false, null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("An unexpected error occurred", false, null));
        }
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<?> getAppointments(@PathVariable Long patientId) {
        try {
            List<AppointmentResponseDTO> appointments = patientService.getAppointmentByPatientId(patientId);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(ex.getMessage(), false, null));
        }
    }

    @GetMapping("/appointments")
    public ResponseEntity<?> getAppointmentsForLoggedInPatient(Principal principal) {
        try {
            List<AppointmentResponseDTO> appointments = patientService.getAppointmentsForLoggedInPatient(principal.getName());
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch appointments", false, null));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getOwnProfile(Principal principal) {
        try {
            PatientResponseDTO profile = patientService.getOwnProfile(principal.getName());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponseDTO<>("Failed to fetch profile", false, null));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponseDTO<?>> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        try {
            patientService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(new ApiResponseDTO<>("Password changed successfully", true, null));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>(ex.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to change password", false, null));
        }


    }

    @GetMapping("/getDoctors/{patientId}")
    public ResponseEntity<?> getDoctorsRelatedToPatient(@PathVariable Long patientId){
        try
        {
            List<PatientResponseDTO.DoctorBasicInfo> doctorInfoList = patientService.getDoctorsRelatedToPatient(patientId);
            return ResponseEntity.ok(new ApiResponseDTO<>("Doctors fetched successfully", true, doctorInfoList));
        }
        catch (Exception e){
            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}