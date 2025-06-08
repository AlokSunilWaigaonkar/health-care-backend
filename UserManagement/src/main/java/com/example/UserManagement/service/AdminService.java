package com.example.UserManagement.service;

import com.example.UserManagement.exception.ResourceNotFoundException;
import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Enums.Status;
import com.example.UserManagement.model.Users.Admin;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.repo.*;
import com.example.UserManagement.request.AddAdminRequest;
import com.example.UserManagement.request.AdminStatsDTO;
import com.example.UserManagement.request.DoctorRegisterRequest;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.response.DoctorResponseDTO;
import com.example.UserManagement.response.PatientResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final DoctorRepo doctorRepo;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final PatientRepo patientRepo;
    private final AppointmentRepository appointmentRepository;
    private final IotRepo iotRepo;

    public void requestAdmin(AddAdminRequest request) {
        // Step 1: Check if the email already exists
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already registered");
        }

        // Step 2: Create a new user in PENDING state
        Admin newAdmin = new Admin();
        newAdmin.setEmail(request.getEmail());
        newAdmin.setPassword(passwordEncoder.encode(request.getPassword())); // Password will be set later
        newAdmin.setRole(Role.ADMIN);
        newAdmin.setAccessLevel(request.getAccessLevel());
        newAdmin.setStatus(Status.PENDING);// Set status to PENDING
        userRepo.save(newAdmin);
    }


    public Doctor registerDoctor(DoctorRegisterRequest request) {
        // Validate the request
        if (request.getFirstName() == null || request.getLastName() == null || request.getEmail() == null) {
            throw new IllegalArgumentException("First name, last name, and email are required");
        }

        // Create doctor entity
        Doctor doctor = new Doctor();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());

        // Generate system credentials for doctor login
        String systemGeneratedEmail = request.getFirstName().toLowerCase() + "." + request.getLastName().toLowerCase() + "@hospital.com";
        String systemGeneratedPassword = UUID.randomUUID().toString().substring(0, 8);
        String hashedPassword = passwordEncoder.encode(systemGeneratedPassword); // Secure password hashing

        // Update doctor entity with system-generated email and hashed password
        doctor.setSystemEmail(systemGeneratedEmail);
        doctor.setSystemPassword(hashedPassword);

        // Save doctor entity
        Doctor savedDoctor = doctorRepo.save(doctor);

        // Send the credentials to the doctor's email
        try {
            sendDoctorCredentialsEmail(doctor.getEmail(), systemGeneratedEmail, systemGeneratedPassword);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send credentials email to doctor", e);
        }

        return savedDoctor;
    }

    private void sendDoctorCredentialsEmail(String doctorEmail, String systemEmail, String password)
            throws MessagingException, UnsupportedEncodingException {
        String subject = "Your System-Generated Login Credentials";
        String senderName = "Hospital Management System";
        String mailContent = "<p>Dear Doctor,</p>"
                + "<p>Your account has been created successfully in our Hospital Management System. Below are your login credentials:</p>"
                + "<p><strong>Email:</strong> " + systemEmail + "</p>"
                + "<p><strong>Password:</strong> " + password + "</p>"
                + "<p>Please log in to your account and change your password immediately for security purposes.</p>"
                + "<p>Thank you,<br>Hospital Management System</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("shadowforgelab@gmail.com", senderName);
        helper.setTo(doctorEmail);
        helper.setSubject(subject);
        helper.setText(mailContent, true); // Enable HTML content

        mailSender.send(message);
    }
    public void approveAdmin(Long userId, boolean approve) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getRole().equals(Role.ADMIN) || !user.getStatus().equals(Status.PENDING)) {
            throw new RuntimeException("Invalid admin request");
        }

        if (approve) {
            user.setStatus(Status.APPROVED);
            user.setEnabled(true);
            String generatedPassword = generateSystemPassword();
            user.setPassword(hashPassword(generatedPassword));
            try {
                sendAdminCredentialsEmail(user.getEmail(), user.getEmail(), generatedPassword);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new RuntimeException("Failed to send admin credentials email: " + e.getMessage());
            }

        } else {
            user.setStatus(Status.REJECTED);
        }
        userRepo.save(user);
    }

    private String generateSystemPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12); // 12-character random password
    }

    private String hashPassword(String password) {
        // Use BCryptPasswordEncoder or any secure hashing algorithm
        return new BCryptPasswordEncoder().encode(password);
    }

    private void sendAdminCredentialsEmail(String adminEmail, String systemEmail, String password)
            throws MessagingException, UnsupportedEncodingException {
        String subject = "Your System-Generated Login Credentials";
        String senderName = "Hospital Management System";
        String mailContent = "<p>Dear Admin,</p>"
                + "<p>Your account has been approved successfully in our Hospital Management System. Below are your login credentials:</p>"
                + "<p><strong>Email:</strong> " + systemEmail + "</p>"
                + "<p><strong>Password:</strong> " + password + "</p>"
                + "<p>Please log in to your account and change your password immediately for security purposes.</p>"
                + "<p>Thank you,<br>Hospital Management System</p>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("shadowforgelab@gmail.com", senderName);
        helper.setTo(adminEmail);
        helper.setSubject(subject);
        helper.setText(mailContent, true);

        mailSender.send(message);
    }

    public List<User> getPendingRequests() {
        return userRepo.findByRoleAndStatus(Role.ADMIN, Status.PENDING);
    }

    public List<DoctorResponseDTO> getAllDoctor(){
        return doctorRepo.findAll().stream().map(doc ->
                new DoctorResponseDTO(
                        doc.getId(),
                        doc.getFirstName(),
                        doc.getLastName(),
                        doc.getEmail(),
                        doc.getSpecialization(),
                        doc.getLicenseNumber(),
                        doc.getQualification(),
                        doc.getPhoneNumber(),
                        doc.getGender(),
                        doc.getAvailabilityHours()
                )
        ).collect(Collectors.toList());
    }

    public List<PatientResponseDTO> getAllPatients(){
        return patientRepo.findAll().stream().map(patient -> {
            List<Doctor> doctors = patient.getDoctors(); // renamed variable for clarity
            List<PatientResponseDTO.DoctorBasicInfo> doctorInfoList = doctors.stream()
                    .map(doc -> new PatientResponseDTO.DoctorBasicInfo(doc.getId(),doc.getFirstName(),doc.getLastName(), doc.getSpecialization()))
                    .toList();

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
                    doctorInfoList
            );
        }).collect(Collectors.toList());
    }

    public void deactivatePatient(Long patientId){
        User patient = userRepo.findById(patientId)
                .orElseThrow(()->new RuntimeException("Patient Not Found"));

        patient.setEnabled(false);
        userRepo.save(patient);
    }

    @Transactional
    public ApiResponseDTO<Void> assignPatientsToDoctor (Long doctorId , Long patientId){
        Optional<Doctor> optionalDoctor = doctorRepo.findById(doctorId);
        if (optionalDoctor.isEmpty()) {
            return new ApiResponseDTO<>("Doctor with ID " + doctorId + " does not exist or is not a doctor.", false,null);
        }

        Optional<Patient> optionalPatient = patientRepo.findById(patientId);
        if (optionalPatient.isEmpty() || !optionalPatient.get().getRole().equals(Role.PATIENT)) {
            return new ApiResponseDTO<>("Patient with ID " + patientId + " does not exist or is not a patient.", false,null);
        }

        Doctor doctor = optionalDoctor.get();
        Patient patient = optionalPatient.get();

        if (doctor.getPatients().contains(patient)) {
            return new ApiResponseDTO<>("Patient with ID " + patientId + " is already assigned to Doctor with ID " + doctorId + ".", false,null);
        }

        patient.getDoctors().add(doctor);
        patientRepo.save(patient);

        return new ApiResponseDTO<>("Patient with ID " + patientId + " has been successfully assigned to Doctor with ID " + doctorId + ".", true,null);

    }

    @Transactional
    public void removeDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Option 1: Delete permanently
         doctorRepo.delete(doctor);
    }

    public AdminStatsDTO getAdminStats() {
        long doctorCount = doctorRepo.count(); // or count()
        long patientCount = patientRepo.countByEnabledTrue();
        long appointmentCount = appointmentRepository.count();

        return new AdminStatsDTO(doctorCount, patientCount, appointmentCount);
    }

    public List<Appointment> getAppointmentHistoryForDoctor(Long doctorId) {
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return appointmentRepository.findByDoctor(doctor);
    }

    @Transactional
    public ApiResponseDTO<Void> deletePatientAndUser(Long patientId) {
        // Fetch the patient or throw exception if not found
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient with ID " + patientId + " not found"));

        patientRepo.deleteDoctorPatientLinksByPatientId(patientId);

        // ✅ Delete all appointments related to the patient
        appointmentRepository.deleteAllByPatientId(patientId);

        iotRepo.deleteAllByPatientId(patientId);

        // ✅ Now safe to delete patient
        patientRepo.deleteById(patientId);

        // ✅ Finally delete user (assuming same ID is used)
        userRepo.deleteById(patientId);

        return new ApiResponseDTO<>("Patient and associated user deleted successfully.", true, null);
    }

    @Transactional
    public ApiResponseDTO<Void> deleteUserById(Long userId) {
        try {
            // 1. Check if user exists
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with ID " + userId + " not found."));

            // 2. Delete role-specific data
            if (user.getRole() == Role.PATIENT) {
                if (!patientRepo.existsById(userId)) {
                    throw new ResourceNotFoundException("Patient data not found for user ID " + userId);
                }
                patientRepo.deleteById(userId);

            } else if (user.getRole() == Role.DOCTOR) {
                if (!doctorRepo.existsById(userId)) {
                    throw new ResourceNotFoundException("Doctor data not found for user ID " + userId);
                }
                doctorRepo.deleteById(userId);
            }

            // 3. Delete user entry
            userRepo.deleteById(userId);

            return new ApiResponseDTO<>("User and associated role-specific data deleted successfully.", true, null);
        } catch (ResourceNotFoundException ex) {
            return new ApiResponseDTO<>(ex.getMessage(), false, null);
        } catch (Exception ex) {
            return new ApiResponseDTO<>("An error occurred while deleting the user: " + ex.getMessage(), false, null);
        }
    }

}