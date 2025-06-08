package com.example.UserManagement.repo;

import com.example.UserManagement.model.AdditionalModel.Appointment;
import com.example.UserManagement.model.Users.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);


    @Query("SELECT CASE WHEN COUNT(a) = 0 THEN true ELSE false END " +
            "FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :appointmentDate")
    boolean isDoctorAvailable(@Param("doctorId") Long doctorId, @Param("appointmentDate") LocalDateTime appointmentDate);

    List<Appointment> findByDoctor(Doctor doctor);

    void deleteAllByPatientId(Long patientId);
}
