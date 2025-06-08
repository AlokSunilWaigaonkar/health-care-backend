package com.example.UserManagement.controller;

import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.request.AddAdminRequest;
import com.example.UserManagement.request.AdminStatsDTO;
import com.example.UserManagement.request.DoctorRegisterRequest;
import com.example.UserManagement.response.*;
import com.example.UserManagement.service.AdminService;
import com.example.UserManagement.service.PatientService;
import com.example.UserManagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AdminService adminService;
    private final PatientService patientService;

    @PostMapping("/register-doctor")
    public ResponseEntity<ApiResponseDTO<Doctor>> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        try {
            Doctor doctor = adminService.registerDoctor(request);
            return ResponseEntity.ok(new ApiResponseDTO<>("Doctor registered successfully",true , doctor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Error registering doctor: " + e.getMessage(), false,null));
        }
    }

    @PostMapping("/request-admin")
    public ResponseEntity<ApiResponseDTO<Void>> requestAdmin(@RequestBody AddAdminRequest request) {
        try {
            adminService.requestAdmin(request);
            return ResponseEntity.ok(new ApiResponseDTO<>("Admin request submitted for approval",  true,null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Failed to request admin access: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/pending-requests")
    public ResponseEntity<ApiResponseDTO<List<User>>> getPendingRequests() {
        try {
            List<User> pendingUsers = adminService.getPendingRequests();
            return ResponseEntity.ok(new ApiResponseDTO<>("Pending admin requests fetched", true, pendingUsers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch pending requests: " + e.getMessage(), false, null));
        }
    }

    @PostMapping("/approve-admin")
    public ResponseEntity<ApiResponseDTO<Void>> approveAdmin(@RequestParam Long userId, @RequestParam boolean approve) {
        try {
            adminService.approveAdmin(userId, approve);
            return ResponseEntity.ok(new ApiResponseDTO<>(
                    approve ? "Admin approved successfully" : "Admin request rejected",  true,null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Error while processing admin approval: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/getUsers")
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getUsers() {
        try {
            List<UserResponseDTO> users = userService.getUsers();
            return ResponseEntity.ok(new ApiResponseDTO<>("All users fetched", true, users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch users: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/get-doctors")
    public ResponseEntity<ApiResponseDTO<List<DoctorResponseDTO>>> getAllDoctors() {
        try {
            List<DoctorResponseDTO> doctors = adminService.getAllDoctor();
            return ResponseEntity.ok(new ApiResponseDTO<>("Doctors fetched", true, doctors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch doctors: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/get-patients")
    public ResponseEntity<ApiResponseDTO<List<PatientResponseDTO>>> getAllPatients() {
        try {
            List<PatientResponseDTO> patients = adminService.getAllPatients();
            return ResponseEntity.ok(new ApiResponseDTO<>("Patients fetched", true, patients));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch patients: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/deactivate-patient")
    public ResponseEntity<ApiResponseDTO<Void>> deactivatePatient(@RequestParam Long patientId) {
        try {
            adminService.deactivatePatient(patientId);
            return ResponseEntity.ok(new ApiResponseDTO<>("Patient Account Deactivated", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Failed to deactivate patient: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponseDTO<List<AppointmentResponseDTO>>> getAppointments(@PathVariable Long patientId) {
        try {
            List<AppointmentResponseDTO> appointments = patientService.getAppointmentByPatientId(patientId);
            return ResponseEntity.ok(new ApiResponseDTO<>("Appointments fetched", true, appointments));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDTO<>(ex.getMessage(), false, null));
        }
    }

    @PostMapping("/assign-patient")
    public ResponseEntity<ApiResponseDTO<Void>> assignPatientToDoctor(
            @RequestParam Long doctorId,
            @RequestParam Long patientId) {
        try {
            ApiResponseDTO<Void> result = adminService.assignPatientsToDoctor(doctorId, patientId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Failed to assign patient: " + e.getMessage(), false, null));
        }
    }

    @DeleteMapping("/remove-doctor/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> removeDoctor(@PathVariable Long id) {
        try {
            adminService.removeDoctor(id);
            return ResponseEntity.ok(new ApiResponseDTO<>("Doctor removed/deactivated successfully", true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDTO<>("Failed to remove doctor: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDTO<AdminStatsDTO>> getAdminStats() {
        try {
            AdminStatsDTO stats = adminService.getAdminStats();
            return ResponseEntity.ok(new ApiResponseDTO<>("Admin stats fetched", true, stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch stats: " + e.getMessage(), false, null));
        }
    }

    @GetMapping("/view-appointment-history/{doctorId}")
    public ResponseEntity<ApiResponseDTO<List<AppointmentResponseDTO>>> getAppointmentHistory(@PathVariable Long doctorId) {
        try {
            List<Appointment> appointments = adminService.getAppointmentHistoryForDoctor(doctorId);
            List<AppointmentResponseDTO> dtos = appointments.stream()
                    .map(AppointmentResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponseDTO<>("Appointment history fetched", true, dtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>("Failed to fetch appointment history: " + e.getMessage(), false, null));
        }
    }
    @DeleteMapping("/delete-patient/{patientId}")
    public ResponseEntity<ApiResponseDTO<Void>> deletePatient(@PathVariable Long patientId) {
        ApiResponseDTO<Void> response = adminService.deletePatientAndUser(patientId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(@PathVariable Long userId) {
        ApiResponseDTO<Void> response = adminService.deleteUserById(userId);
        return ResponseEntity.ok(response);
    }
}