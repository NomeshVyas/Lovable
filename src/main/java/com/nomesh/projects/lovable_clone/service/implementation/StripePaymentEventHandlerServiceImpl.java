package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.PaymentException;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.service.PaymentEventHandlerService;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StripePaymentEventHandlerServiceImpl implements PaymentEventHandlerService {

    UserRepository userRepository;
    SubscriptionService subscriptionService;
    PlanRepository planRepository;

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.debug("Handling stripe event: {}", type);

        switch (type) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata);
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject);
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject);
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
            default -> log.debug("Unhandled Stripe event type: {}", type);
        }
    }

    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
        if (session == null) {
            log.error("session is null in handleCheckoutSessionCompleted");
            return;
        }
        String planIdRaw = metadata.get("plan_id");
        String userIdRaw = metadata.get("user_id");
        if (planIdRaw == null || userIdRaw == null) {
            log.error("Missing metadata on checkout session {}: plan_id={}, user_id={}", session.getId(), planIdRaw, userIdRaw);
            return;
        }

        Long planId = Long.parseLong(planIdRaw);
        Long userId = Long.parseLong(userIdRaw);

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        User user = userRepository.getByIdOrThrow(userId);
        if (user.getPaymentCustomerId() == null) {
            user.setPaymentCustomerId(customerId);
            userRepository.save(user);
        }

        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }

    private void handleCustomerSubscriptionUpdated(Subscription subscription) {
        if (subscription == null) {
            log.warn("Subscription object is null in stripe for subscription update inside handleCustomerSubscriptionUpdated");
            return;
        }

        SubscriptionStatus subscriptionStatus = mapStripeStatusToSubscriptionStatusEnum(subscription.getStatus());
        if (subscriptionStatus == null) {
            log.warn("Unknown status '{}' for subscription {}", subscription.getStatus(), subscription.getId());
            return;
        }

        SubscriptionItem subscriptionItem = subscription.getItems().getData().getFirst();
        Instant periodStart = toInstant(subscriptionItem.getCurrentPeriodStart());
        Instant periodEnd = toInstant(subscriptionItem.getCurrentPeriodEnd());
        Long planId = resolvePlanId(subscriptionItem.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), subscriptionStatus, periodStart,
                periodEnd, planId, subscription.getCancelAtPeriodEnd()
        );
    }

    private void handleCustomerSubscriptionDeleted(Subscription subscription) {
        if (subscription == null) {
            log.error("Subscription object is null in stripe for subscription delete inside handleCustomerSubscriptionDeleted");
            return;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaid(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            SubscriptionItem subscriptionItem = subscription.getItems().getData().getFirst();
            Instant periodStart = toInstant(subscriptionItem.getCurrentPeriodStart());
            Instant periodEnd = toInstant(subscriptionItem.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);

        } catch (StripeException e) {
            log.error("Failed to retrieve subscription {}", subscriptionId, e);
            throw new PaymentException("Unable to renew subscription period");
        }
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    private SubscriptionStatus mapStripeStatusToSubscriptionStatusEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELLED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped stripe status: {}", status);
                yield null;
            }
        };
    }

    private Long resolvePlanId(Price price) {
        if (price == null || price.getId() == null) return null;
        return planRepository.findByPaymentPriceId(price.getId())
                .map(com.nomesh.projects.lovable_clone.entity.Plan::getId)
                .orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice) {
        Invoice.Parent invoiceParent = invoice.getParent();
        if (invoiceParent == null) return null;

        Invoice.Parent.SubscriptionDetails subscriptionDetails = invoiceParent.getSubscriptionDetails();
        if (subscriptionDetails == null) return null;

        return subscriptionDetails.getSubscription();
    }

    private Instant toInstant(Long epoch) {
        return epoch != null ? Instant.ofEpochSecond(epoch) : null;
    }
}
