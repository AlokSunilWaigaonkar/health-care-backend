package com.example.UserManagement.service;

import com.example.UserManagement.config.JwtTokenUtil;
import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.AuthResponse;
import com.example.UserManagement.model.Enums.AppointmentStatus;
import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.repo.AppointmentRepository;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.PatientRepo;
import com.example.UserManagement.repo.UserRepo;
import com.example.UserManagement.request.AppointmentRequest;
import com.example.UserManagement.request.DoctorUpdateRequest;
import com.example.UserManagement.response.AppointmentResponseDTO;
import com.example.UserManagement.response.PatientResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final PatientRepo patientRepo;
    private final AppointmentRepository appointmentRepository;

    public AuthResponse login(String systemEmail , String systemPassword){
        // Step 1: Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(systemEmail, systemPassword)
        );

        // Step 2: Set authentication context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: Generate tokens
        String token = jwtTokenUtil.generateToken(systemEmail);
        String refreshToken = jwtTokenUtil.generateRefreshToken(systemEmail);

        // Step 4: Fetch Doctor or throw exception
        Doctor doctor = doctorRepo.findBySystemEmail(systemEmail)
                .orElseThrow(() -> new RuntimeException("Doctor not found with email: " + systemEmail));

        // Step 5: Build AuthResponse
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(token);         // ✅ Access token
        authResponse.setRefreshToken(refreshToken); // ✅ Refresh token
        authResponse.setUserId(doctor.getId());     // ✅ Doctor ID

        // Step 6: Set role from granted authorities (usually 'DOCTOR')
        String authority = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("PATIENT"); // Fallback if not found

        authResponse.setRole(Role.valueOf(authority));

        return authResponse;
    }
    public List<PatientResponseDTO> getAllPatientsOfDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return doctor.getPatients().stream().map(patient -> {
                    PatientResponseDTO.DoctorBasicInfo doctorInfo = new PatientResponseDTO.DoctorBasicInfo(
                            doctor.getId(), doctor.getFirstName(), doctor.getLastName(), doctor.getSpecialization()
                    );

                    return new PatientResponseDTO(
                            patient.getId(),
                            patient.getEmail(),
                            patient.getFirstName(),
                            patient.getLastName(),
                            patient.getAge(),
                            patient.getGender(),
                            patient.getDateOfBirth(),
                            patient.getAddress(),
                            patient.getBloodGroup(),
                            patient.getEmergencyContact(),
                            patient.getContactNo(),
                            patient.getMedicalHistory(),
                            List.of(doctorInfo)
                    );
                }).collect(Collectors.toList())
                .reversed();
    }

    public PatientResponseDTO getPatientData (Long doctorId ,  Long patientId){
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Optional<Patient> patient = doctor.getPatients().stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst();

        Patient p = patient.orElseThrow(() -> new RuntimeException("Patient not found or not associated with this doctor"));

        PatientResponseDTO.DoctorBasicInfo doctorInfo = new PatientResponseDTO.DoctorBasicInfo(
                doctor.getId(),doctor.getFirstName(), doctor.getLastName(), doctor.getSpecialization());

        return new PatientResponseDTO(
                p.getId(),
                p.getEmail(),
                p.getFirstName(),
                p.getLastName(),
                p.getAge(),
                p.getGender(),
                p.getDateOfBirth(),
                p.getAddress(),
                p.getBloodGroup(),
                p.getEmergencyContact(),
                p.getContactNo(),
                p.getMedicalHistory(),
                List.of(doctorInfo)
        );

    }

    public AppointmentResponseDTO bookAppointment(Long doctorId , Long patientId , AppointmentRequest request){
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new RuntimeException("Something went wrong"));
        Patient patient = patientRepo.findById(patientId).orElseThrow(() -> new RuntimeException("Something went wrong"));

        if (!doctor.getPatients().contains(patient)) {
            throw new RuntimeException("Patient is not associated with this doctor");
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setReason(request.getReason());
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        appointment = appointmentRepository.save(appointment);

        // Map to DTO before returning
        AppointmentResponseDTO.DoctorInfo doctorInfo = new AppointmentResponseDTO.DoctorInfo(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization()
        );

        AppointmentResponseDTO.PatientInfo patientInfo = new AppointmentResponseDTO.PatientInfo(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getAge(),
                patient.getGender()
        );

        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getAppointmentDate(),
                appointment.getReason(),
                appointment.getStatus(),
                doctorInfo,
                patientInfo
        );
    }

    public List<AppointmentResponseDTO> getAppointmentsForDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<Appointment> appointments = appointmentRepository.findByDoctor(doctor);

        return appointments.stream().map(appointment -> {
            Doctor doc = appointment.getDoctor();
            Patient pat = appointment.getPatient();

            AppointmentResponseDTO.DoctorInfo doctorInfo = new AppointmentResponseDTO.DoctorInfo(
                    doc.getId(),
                    doc.getFirstName(),
                    doc.getLastName(),
                    doc.getSpecialization()
            );

            AppointmentResponseDTO.PatientInfo patientInfo = new AppointmentResponseDTO.PatientInfo(
                    pat.getId(),
                    pat.getFirstName(),
                    pat.getLastName(),
                    pat.getAge(),
                    pat.getGender()
            );

            return new AppointmentResponseDTO(
                    appointment.getId(),
                    appointment.getAppointmentDate(),
                    appointment.getReason(),
                    appointment.getStatus() != null ? appointment.getStatus() : null,
                    doctorInfo,
                    patientInfo
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void updateAppointmentStatus(Long appointmentId, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (status == AppointmentStatus.COMPLETED) {
            // Delete appointment if status is COMPLETED
            appointmentRepository.delete(appointment);
        } else {
            // Otherwise, just update the status
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
        }
    }
    public void updateDoctorProfile(Long doctorId, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setQualification(request.getQualification());
        doctor.setAvailabilityHours(request.getAvailabilityHours());

        doctorRepo.save(doctor);
    }
    public Doctor getDoctorById(Long doctorId) {
        return doctorRepo.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public void setAvailability(Long doctorId, String availabilityHours) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new RuntimeException("Doctor not found"));
        doctor.setAvailabilityHours(availabilityHours);
        doctorRepo.save(doctor);
    }

    public String getAvailability(Long doctorId) {
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"))
                .getAvailabilityHours();
    }

    public List<PatientResponseDTO> searchByName(String name){
        return patientRepo.findByFirstNameContainingOrLastNameContaining(name, name);
    }

}
