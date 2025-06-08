package com.example.UserManagement.repo;

import com.example.UserManagement.model.AdditionalModel.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepo  extends JpaRepository<NotificationModel,Long> {
}
