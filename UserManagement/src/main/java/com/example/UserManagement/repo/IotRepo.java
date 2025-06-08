package com.example.UserManagement.repo;

import com.example.UserManagement.model.AdditionalModel.IotData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IotRepo extends JpaRepository<IotData,Long> {
    List<IotData> findByPatientIdAndCreatedAtAfter(Long patientId, LocalDateTime sevenDaysAgo);

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.appointmentDate < :cutOfDate")
    void deleteOldEntries(LocalDateTime cutOfDate);

    List<IotData> findTop7ByPatientIdOrderByCreatedAtDesc(Long patientId);

    void deleteAllByPatientId(Long patientId);
}
