package com.example.UserManagement.controller;

import com.example.UserManagement.model.AuthResponse;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.request.*;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.response.AppointmentResponseDTO;
import com.example.UserManagement.response.DoctorResponseDTO;
import com.example.UserManagement.response.PatientResponseDTO;
import com.example.UserManagement.service.DoctorService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<AuthResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        try {
            AuthResponse response = doctorService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(new ApiResponseDTO<>("Doctor login successful", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Login failed due to an internal error"+ e.getMessage(), false,null));
        }
    }

    @GetMapping("/{doctorId}/patients")
    public ResponseEntity<ApiResponseDTO<List<PatientResponseDTO>>> getAllPatients(@PathVariable Long doctorId) {
        try {
            List<PatientResponseDTO> patients = doctorService.getAllPatientsOfDoctor(doctorId);
            return ResponseEntity.ok(new ApiResponseDTO<>("Patients retrieved successfully", true, patients));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO<>("Failed to fetch patients"+e.getMessage(), false, null));
        }
    }

    @GetMapping("/{doctorId}/patients/{patientId}")
    public ResponseEntity<ApiResponseDTO<PatientResponseDTO>> getPatientData(@PathVariable Long doctorId, @PathVariable Long patientId) {
        try {
            PatientResponseDTO patient = doctorService.getPatientData(doctorId, patientId);
            return ResponseEntity.ok(new ApiResponseDTO<>("Patient data fetched", true, patient));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ApiResponseDTO<>("Patient not found"+e.getMessage(), false,null));
        }
    }

    @PostMapping("/{doctorId}/patients/{patientId}/appointments")
    public ResponseEntity<ApiResponseDTO<AppointmentResponseDTO>> bookAppointment(@PathVariable Long doctorId, @PathVariable Long patientId, @RequestBody AppointmentRequest appointmentRequest) {
        try {
            AppointmentResponseDTO booked = doctorService.bookAppointment(doctorId, patientId, appointmentRequest);
            return ResponseEntity.ok(new ApiResponseDTO<>("Appointment booked successfully", true, booked));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ApiResponseDTO<>("Appointment booking failed"+ e.getMessage(), false,null));
        }
    }

    @GetMapping("/{doctorId}/appointments")
    public ResponseEntity<ApiResponseDTO<List<AppointmentResponseDTO>>> getDoctorAppointments(@PathVariable Long doctorId) {
        List<AppointmentResponseDTO> appointments = doctorService.getAppointmentsForDoctor(doctorId);
        return ResponseEntity.ok(new ApiResponseDTO<>("Appointments fetched successfully", true, appointments));
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public ResponseEntity<ApiResponseDTO<Void>> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody UpdateAppointmentStatusRequest status) {
        try {
            doctorService.updateAppointmentStatus(appointmentId, status.getStatus());
            return ResponseEntity.ok(new ApiResponseDTO<>("Appointment status updated successfully", true, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Failed to update appointment status"+ e.getMessage(), false,null));
        }
    }

    @PostMapping("/update-appointment-status")
    public ResponseEntity<ApiResponseDTO<Void>> updateAppointmentStatus(@RequestBody UpdateAppointmentStatusRequest request) {
        doctorService.updateAppointmentStatus(request.getAppointmentId(), request.getStatus());
        return ResponseEntity.ok(new ApiResponseDTO<>("Appointment status updated successfully", true, null));
    }

    @GetMapping("/profile/{doctorId}")
    public ResponseEntity<ApiResponseDTO<DoctorResponseDTO>> getDoctorProfile(@PathVariable Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        return ResponseEntity.ok(new ApiResponseDTO<>("Doctor profile fetched", true, new DoctorResponseDTO(doctor)));
    }

    @PutMapping("/update-profile/{doctorId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateDoctorProfile(@PathVariable Long doctorId, @RequestBody DoctorUpdateRequest request) {
        doctorService.updateDoctorProfile(doctorId, request);
        return ResponseEntity.ok(new ApiResponseDTO<>("Doctor profile updated successfully", true, null));
    }

    @PostMapping("/set-availability/{doctorId}")
    public ResponseEntity<ApiResponseDTO<Void>> setAvailability(@PathVariable Long doctorId, @RequestBody AvailabilityRequest request) {
        doctorService.setAvailability(doctorId, request.getAvailabilityHours());
        return ResponseEntity.ok(new ApiResponseDTO<>("Availability updated successfully", true, null));
    }
    @GetMapping("/availability/{doctorId}")
    public ResponseEntity<ApiResponseDTO<String>> getAvailability(@PathVariable Long doctorId) {
        String availability = doctorService.getAvailability(doctorId);
        return ResponseEntity.ok(new ApiResponseDTO<>("Availability fetched successfully", true, availability));
    }

    @PostMapping("/search-by-name")
    public ResponseEntity<ApiResponseDTO<?>> searchByName(@RequestBody String name){
       return ResponseEntity.ok(new ApiResponseDTO<>("Patients fetched with name "+name,true, doctorService.searchByName(name)));
    }
}