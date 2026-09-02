package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.entity.Subscription;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.IncorrectResultSizeDataAccessException;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.SubscriptionMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.repository.ProjectMemberRepository;
import com.nomesh.projects.lovable_clone.repository.SubscriptionRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    AuthUtil authUtil;
    SubscriptionRepository subscriptionRepository;
    SubscriptionMapper subscriptionMapper;
    UserRepository userRepository;
    PlanRepository planRepository;
    ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();

        Subscription subscription = getCurrentSubscriptionHelper(userId);
        if (subscription == null)
            throw new ResourceNotFoundException("Subscription", userId);

        return subscriptionMapper.toSubscriptionResponse(subscription);
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
    public void updateSubscription(String paymentSubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Long planId, Boolean cancelAtPeriodEnd) {
        Subscription subscription = subscriptionRepository.getByPaymentSubscriptionIdOrThrow(paymentSubscriptionId);
        boolean isSubscriptionUpdated = false;

        if (subscriptionStatus != null && subscriptionStatus != subscription.getStatus()){
            subscription.setStatus(subscriptionStatus);
            isSubscriptionUpdated = true;
        }
        if (periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())) {
            subscription.setCurrentPeriodStart(periodStart);
            isSubscriptionUpdated = true;
        }
        if (periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())) {
            subscription.setCurrentPeriodEnd(periodEnd);
            isSubscriptionUpdated = true;
        }
        if (cancelAtPeriodEnd != null && !cancelAtPeriodEnd.equals(subscription.getCancelAtPeriodEnd())) {
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            isSubscriptionUpdated = true;
        }
        if (planId != null && !planId.equals(subscription.getPlan().getId())) {
            Plan updatedPlan = planRepository.getByIdOrThrow(planId);
            subscription.setPlan(updatedPlan);
            isSubscriptionUpdated = true;
        }

        if (isSubscriptionUpdated) {
            log.debug("Subscription has been updated: {}", paymentSubscriptionId);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscription(String paymentSubscriptionId) {
        Subscription subscription = subscriptionRepository.getByPaymentSubscriptionIdOrThrow(paymentSubscriptionId);

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            log.debug("Subscription is already CANCELLED for paymentSubscriptionId: {}", paymentSubscriptionId);
            return;
        }
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
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
        Subscription subscription = subscriptionRepository.getByPaymentSubscriptionIdOrThrow(paymentSubscriptionId);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            log.debug("Subscription is already PAST_DUE for paymentSubscriptionId: {}", paymentSubscriptionId);
            return;
        }

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        // Notify user via email...
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateNewProject() {
        Long userId = authUtil.getCurrentUserId();
        Subscription currentSubscription = getCurrentSubscriptionHelper(userId);
        long countOfOwnedProjects = projectMemberRepository.countProjectsOwnedByUser(userId);

        if (currentSubscription == null || currentSubscription.getPlan() == null) {
            Long freeTierProjectsAllowed = planRepository.getMaxProjectsForFreePlan().orElseThrow(() ->
                    new IncorrectResultSizeDataAccessException("Max Projects count not found for free plan")
                );
            return countOfOwnedProjects < freeTierProjectsAllowed;
        }

        return countOfOwnedProjects < currentSubscription.getPlan().getMaxProjects();
    }

    private Subscription getCurrentSubscriptionHelper(Long userId) {
        return subscriptionRepository.findByUserIdAndStatusIn(
                userId,
                Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE,
                    SubscriptionStatus.TRIALING
                )
        ).orElse(null);
    }
}
