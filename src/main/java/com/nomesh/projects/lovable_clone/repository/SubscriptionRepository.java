package com.nomesh.projects.lovable_clone.repository;

import com.nomesh.projects.lovable_clone.entity.Subscription;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatusIn(Long userId, Set<SubscriptionStatus> statusSet);

    boolean existsByPaymentSubscriptionId(String subscriptionId);

    Optional<Subscription> findByPaymentSubscriptionId(String paymentSubscriptionId);

    default Subscription getByPaymentSubscriptionIdOrThrow(String paymentSubscriptionId) {
        return findByPaymentSubscriptionId(paymentSubscriptionId).orElseThrow(() ->
                new ResourceNotFoundException("Subscription", paymentSubscriptionId)
        );
    }
}
