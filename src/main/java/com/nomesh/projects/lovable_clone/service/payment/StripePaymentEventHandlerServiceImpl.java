package com.nomesh.projects.lovable_clone.service.payment;

import com.nomesh.projects.lovable_clone.config.StripeProperties;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.exception.PaymentException;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.service.subscription.SubscriptionService;
import com.nomesh.projects.lovable_clone.service.user.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.jspecify.annotations.NonNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StripePaymentEventHandlerServiceImpl implements PaymentEventHandlerService {

    UserService userService;
    SubscriptionService subscriptionService;
    PlanRepository planRepository;
    StripeProperties stripeProperties;

    @Override
    public void handleWebhookEvent(String payload, String signatureHeader) {
        Event event = verifyAndParseEvent(payload, signatureHeader);
        String type = event.getType();

        log.debug("Handling stripe event: {} (id={})", type, event.getId());

        Optional<StripeObject> optionalStripeObject = extractStripeObjectFromStripeEvent(event);
        if (optionalStripeObject.isEmpty()) {
            log.warn("Skipping event {} (id={}): payload could not be deserialized", type, event.getId());
            return;
        }

        StripeObject stripeObject = optionalStripeObject.get();

        switch (type) {
            case "checkout.session.completed" ->
                    asType(stripeObject, Session.class, type)
                        .ifPresent(this::handleCheckoutSessionCompleted);
            case "customer.subscription.updated" ->
                    asType(stripeObject, Subscription.class, type)
                        .ifPresent(this::handleCustomerSubscriptionUpdated);
            case "customer.subscription.deleted" ->
                    asType(stripeObject, Subscription.class, type)
                        .ifPresent(this::handleCustomerSubscriptionDeleted);
            case "invoice.paid" ->
                    asType(stripeObject, Invoice.class, type)
                        .ifPresent(this::handleInvoicePaid);
            case "invoice.payment_failed" ->
                    asType(stripeObject, Invoice.class, type)
                        .ifPresent(this::handleInvoicePaymentFailed);
            default -> log.debug("Unhandled Stripe event type: {}", type);
        }
    }

    private void handleCheckoutSessionCompleted(@NonNull Session session) {
        Map<String, String> metadata = session.getMetadata() != null ? session.getMetadata() : Map.of();
        Long planId = parseId(metadata.get("plan_id"), "plan_id", session.getId());
        Long userId = parseId(metadata.get("user_id"), "user_id", session.getId());

        if (planId == null || userId == null) return;

        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        userService.assignPaymentCustomerIdIfAbsent(userId, customerId);

        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }

    private void handleCustomerSubscriptionUpdated(@NonNull Subscription subscription) {
        SubscriptionStatus subscriptionStatus = mapStripeStatusToSubscriptionStatusEnum(subscription.getStatus());
        if (subscriptionStatus == null) {
            log.warn("Unknown status '{}' for subscription {}", subscription.getStatus(), subscription.getId());
            return;
        }

        Optional<SubscriptionItem> optionalSubscriptionItem = firstItemOf(subscription);
        if (optionalSubscriptionItem.isEmpty()) return;

        SubscriptionItem subscriptionItem = optionalSubscriptionItem.get();
        Instant periodStart = toInstant(subscriptionItem.getCurrentPeriodStart());
        Instant periodEnd = toInstant(subscriptionItem.getCurrentPeriodEnd());
        Long planId = resolvePlanId(subscriptionItem.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), subscriptionStatus, periodStart,
                periodEnd, planId, subscription.getCancelAtPeriodEnd()
        );
    }

    private void handleCustomerSubscriptionDeleted(@NonNull Subscription subscription) {
        subscriptionService.cancelSubscription(subscription.getId());
    }

    private void handleInvoicePaid(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Optional<SubscriptionItem> optionalSubscriptionItem = firstItemOf(subscription);
            if (optionalSubscriptionItem.isEmpty()) return;

            SubscriptionItem subscriptionItem = optionalSubscriptionItem.get();
            Instant periodStart = toInstant(subscriptionItem.getCurrentPeriodStart());
            Instant periodEnd = toInstant(subscriptionItem.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);
        } catch (StripeException exception) {
            log.error("Failed to retrieve subscription {}", subscriptionId, exception);
            throw new PaymentException("Unable to renew subscription period", exception);
        }
    }

    private void handleInvoicePaymentFailed(Invoice invoice) {
        String subscriptionId = extractSubscriptionId(invoice);
        if (subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    private Event verifyAndParseEvent(String payload, String signatureHeader) {
        try {
            return Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecretKey());
        } catch (SignatureVerificationException exception) {
            log.warn("Rejected webhook: invalid Stripe signature", exception);
            throw new BadRequestException("Invalid webhook signature");
        }
    }

    private Optional<StripeObject> extractStripeObjectFromStripeEvent(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        Optional<StripeObject> stripeObject = deserializer.getObject();
        if (stripeObject.isPresent()) return stripeObject;

        // Fallback: Deserialize from raw JSON
        try {
            return Optional.ofNullable(deserializer.deserializeUnsafe());
        } catch (Exception exception) {
            log.error("Unsafe deserialization failed for event {}", event.getType(), exception);
            return Optional.empty();
        }
    }

    private <T extends StripeObject> Optional<T> asType(StripeObject stripeObject, Class<T> expectedType, String eventType) {
        if (expectedType.isInstance(stripeObject))
            return Optional.of(expectedType.cast(stripeObject));

        return Optional.empty();
    }

    private Long parseId(String rawValue, String field, String sessionId) {
        if (rawValue == null) {
            log.error("Missing '{}' metadata on checkout session {}", field, sessionId);
            return null;
        }

        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException exception) {
            log.error("Invalid '{}' metadata '{}' on checkout session {}", field, rawValue, sessionId);
            return null;
        }
    }

    private Optional<SubscriptionItem> firstItemOf(Subscription subscription) {
        SubscriptionItemCollection itemCollection = subscription.getItems();

        if (itemCollection == null || itemCollection.getData() == null || itemCollection.getData().isEmpty()) {
            log.warn("Subscription {} arrived with no items", subscription.getId());
            return Optional.empty();
        }

        return Optional.of(itemCollection.getData().getFirst());
    }

    private SubscriptionStatus mapStripeStatusToSubscriptionStatusEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELLED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            case null, default -> {
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
