package com.example.UserManagement.repo;

import com.example.UserManagement.model.Users.Patient;
import com.example.UserManagement.response.PatientResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepo extends JpaRepository<Patient,Long> {
    Optional<Patient> findByEmail(String email);

    long countByEnabledTrue();

    List<PatientResponseDTO> findByFirstNameContainingOrLastNameContaining(String name, String name1);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM doctor_patient WHERE patient_id = :patientId", nativeQuery = true)
    void deleteDoctorPatientLinksByPatientId(@Param("patientId") Long patientId);

}
