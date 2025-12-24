package com.petlifecycle.petlifecycle_backend.repository;

import com.petlifecycle.petlifecycle_backend.model.ApplicationTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTransitionLogRepository extends JpaRepository<ApplicationTransitionLog, Long> {
}
