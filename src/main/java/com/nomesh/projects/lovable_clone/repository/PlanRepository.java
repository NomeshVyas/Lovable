package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByPaymentPriceId(String paymentPriceId);

    default Plan getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan", id));
    }
}
