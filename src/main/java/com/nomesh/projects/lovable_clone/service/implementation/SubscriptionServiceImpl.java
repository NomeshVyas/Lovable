package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.entity.Subscription;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.SubscriptionMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.repository.SubscriptionRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SubscriptionServiceImpl implements SubscriptionService {

    AuthUtil authUtil;
    SubscriptionRepository subscriptionRepository;
    SubscriptionMapper subscriptionMapper;
    UserRepository userRepository;
    PlanRepository planRepository;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();

        return subscriptionMapper.toSubscriptionResponse(
            subscriptionRepository.findByUserIdAndStatusIn(userId, Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE,
                    SubscriptionStatus.TRIALING
            )).orElseThrow(() ->
                    new ResourceNotFoundException("Subscription", userId)
            )
        );
    }

    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {
        boolean alreadyExists = subscriptionRepository.existsByPaymentSubscriptionId(subscriptionId);
        if (alreadyExists) return;

        User user = userRepository.getByIdOrThrow(userId);
        Plan plan = planRepository.getByIdOrThrow(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .paymentSubscriptionId(subscriptionId)
                .status(SubscriptionStatus.INCOMPLETE)
                .build();

        subscriptionRepository.save(subscription);
    }

    @Override
    public void updateSubscription(String id, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Long planId, Boolean cancelAtPeriodEnd) {

    }

    @Override
    public void cancelSubscription(String subscriptionId) {

    }

    @Override
    public void renewSubscriptionPeriod(String paymentSubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = subscriptionRepository.getByPaymentSubscriptionIdOrThrow(paymentSubscriptionId);

        Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE)
            subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);
    }

    @Override
    public void markSubscriptionPastDue(String paymentSubscriptionId) {

    }
}
