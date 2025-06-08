package com.example.UserManagement.service;


import com.example.UserManagement.exception.UserAlreadyExistsException;
import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.Enums.Role;
import com.example.UserManagement.model.Users.Doctor;
import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.model.Users.User;
import com.example.UserManagement.repo.AppointmentRepository;
import com.example.UserManagement.repo.DoctorRepo;
import com.example.UserManagement.repo.PatientRepo;
import com.example.UserManagement.repo.UserRepo;
import com.example.UserManagement.request.AppointmentRequest;
import com.example.UserManagement.request.ChangePasswordRequest;
import com.example.UserManagement.request.PatientUpdateRequest;
import com.example.UserManagement.response.ApiResponseDTO;
import com.example.UserManagement.response.AppointmentResponseDTO;
import com.example.UserManagement.response.PatientResponseDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.UserManagement.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class PatientService {

    private final PatientRepo patientRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepo doctorRepo;


    public void updatePatientDetails(Long patientId, PatientUpdateRequest patientUpdateRequest){
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not Found"));

        /*
         private String FirstName;
    private String LastName;
    private int age;
    private String gender;
    private String bloodGroup;
    private String emergencyContact;
    private Date DateOfBirth;
    private String Address;
    private String ContactNo;
    private String healthRecords;
    private String MedicalHistory;
        */

        patient.setFirstName(patientUpdateRequest.getFirstName());
        patient.setLastName(patientUpdateRequest.getLastName());
        patient.setAddress(patientUpdateRequest.getAddress());
        patient.setAge(patientUpdateRequest.getAge());
        patient.setBloodGroup(patientUpdateRequest.getBloodGroup());
        patient.setGender(patientUpdateRequest.getGender());
        patient.setEmergencyContact(patientUpdateRequest.getEmergencyContact());
        patient.setContactNo(patientUpdateRequest.getContactNo());
        patient.setMedicalHistory(patientUpdateRequest.getMedicalHistory());
        patient.setDateOfBirth(patientUpdateRequest.getDateOfBirth());

        patientRepo.save(patient);

        new ApiResponseDTO<>("Patient details updated successfully.", true, null);

    }
    public User registerPatient(RegisterRequest request) {

        Optional<Patient> user = patientRepo.findByEmail(request.getEmail());
        if(user.isPresent()){
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + "already exists");
        }
        Patient newUser = new Patient();
        newUser.setEmail(request.getEmail());
        String role = request.getRole();
        Role userRole = (role != null && !role.isEmpty()) ? Role.valueOf(role) : Role.PATIENT;
        newUser.setRole(userRole);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        return patientRepo.save(newUser);
    }

    public void bookAppointment(Long doctorId, Long patientId , AppointmentRequest appiontmentRequest){
        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        boolean isAvailable = appointmentRepository.isDoctorAvailable(doctor.getId(),appiontmentRequest.getAppointmentDate());
        if (!isAvailable) {
            new ApiResponseDTO<>("Doctor is not available at the requested time", false, null);
            return;
        }
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(appiontmentRequest.getAppointmentDate());
        appointment.setReason(appiontmentRequest.getReason());
        appointment.setStatus(appiontmentRequest.getStatus());
        appointmentRepository.save(appointment);

    }

    public List<AppointmentResponseDTO> getAppointmentByPatientId(Long patientId){
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);

        return appointments.stream().map(appointment -> {
            AppointmentResponseDTO.DoctorInfo doctorInfo = new AppointmentResponseDTO.DoctorInfo(
                    appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName(),
                    appointment.getDoctor().getSpecialization()
            );

            AppointmentResponseDTO.PatientInfo patientInfo = new AppointmentResponseDTO.PatientInfo(
                    patient.getFirstName(),
                    patient.getLastName()
            );

            return new AppointmentResponseDTO(
                    appointment.getId(),
                    appointment.getAppointmentDate(),
                    appointment.getReason(),
                    appointment.getStatus(),
                    doctorInfo,
                    patientInfo
            );
        }).collect(Collectors.toList());
    }

    public List<AppointmentResponseDTO> getAppointmentsForLoggedInPatient(String email) {
        Patient patient = patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());

        return appointments.stream()
                .map(AppointmentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public PatientResponseDTO getOwnProfile(String email) {
        Patient patient = patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return new PatientResponseDTO(patient);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        Patient patient = patientRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), patient.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        patient.setPassword(passwordEncoder.encode(request.getNewPassword()));
        patientRepo.save(patient);
    }
    public List<PatientResponseDTO.DoctorBasicInfo> getDoctorsRelatedToPatient(Long patientId){
        Patient  patient = patientRepo.findById(patientId).orElseThrow(()->new RuntimeException("Patient Not found"));
        List<Doctor> doctors = patient.getDoctors();
        return doctors.stream()
                .map(doc -> new PatientResponseDTO.DoctorBasicInfo(doc.getId(),doc.getFirstName(),doc.getLastName(), doc.getSpecialization()))
                .toList();
    }

}
