package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByPaymentPriceId(String paymentPriceId);

    List<Plan> findByActiveTrueAndPaymentPriceIdIsNotNull();

    @Query("""
        SELECT p.maxProjects FROM Plan p
        WHERE p.paymentPriceId IS NULL
    """)
    Optional<Long> getMaxProjectsForFreePlan();

    default Plan getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan", id));
    }
}
