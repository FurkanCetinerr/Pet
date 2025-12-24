package com.petlifecycle.petlifecycle_backend.repository;

import com.petlifecycle.petlifecycle_backend.model.Application;
import com.petlifecycle.petlifecycle_backend.model.ApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByBasvuruDurumuIn(List<ApplicationStatus> statuses);
}
