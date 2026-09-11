package com.nomesh.projects.lovable_clone.service.payment;

import com.nomesh.projects.lovable_clone.config.ClientProperties;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.exception.PaymentException;
import com.nomesh.projects.lovable_clone.mapper.SubscriptionMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.service.user.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StripePaymentProcessorServiceImpl implements PaymentProcessorService {

    PlanRepository planRepository;
    SubscriptionMapper subscriptionMapper;
    UserService userService;
    ClientProperties clientProperties;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest) {
        Plan plan = planRepository.getByIdOrThrow(checkoutRequest.planId());
        User user = userService.getCurrentUser();
        String paymentCustomerId = getOrCreatePaymentCustomerId(user);
        SessionCreateParams sessionCreateParams = buildSessionCreateParams(user, plan, paymentCustomerId);

        try {
                Session session = Session.create(sessionCreateParams);
                return subscriptionMapper.toCheckoutResponse(session.getUrl());
        } catch (StripeException exception) {
                throw new PaymentException("Unable to create Stripe checkout session", exception);
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        User user = userService.getCurrentUser();
        String paymentCustomerId = user.getPaymentCustomerId();

        if (paymentCustomerId == null || paymentCustomerId.isEmpty())
            throw new BadRequestException("User does not have a Stripe Customer Id, userId: " + user.getId());

        String portalSessionUrl = createPortalSession(paymentCustomerId).getUrl();
        return subscriptionMapper.toPortalResponse(portalSessionUrl);
    }

    private SessionCreateParams buildSessionCreateParams(User user, Plan plan, String paymentCustomerId) {
        SubscriptionData subscriptionData = SubscriptionData.builder()
                .setBillingMode(
                        SubscriptionData.BillingMode.builder()
                                .setType(SubscriptionData.BillingMode.Type.FLEXIBLE)
                                .build()
                )
                .build();

        return SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(plan.getPaymentPriceId())
                                .setQuantity(1L)
                                .build()
                )
                .setSubscriptionData(subscriptionData)
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(clientProperties.frontendUrl() + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(clientProperties.frontendUrl() + "/cancel.html")
                .putMetadata("plan_id", plan.getId().toString())
                .putMetadata("user_id", user.getId().toString())
                .setCustomer(paymentCustomerId)
                .build();
    }

    private String getOrCreatePaymentCustomerId(User user) {
        Long userId = user.getId();
        String paymentCustomerId = user.getPaymentCustomerId();

        if (paymentCustomerId != null && !paymentCustomerId.isEmpty())
            return paymentCustomerId;

        try {
            Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(user.getEmail())
                            .setName(user.getName())
                            .putMetadata("user_id", userId.toString())
                            .build()
            );
            paymentCustomerId = customer.getId();
        } catch (StripeException exception) {
            throw new PaymentException("Unable to create Stripe customer for user id: " + userId, exception);
        }

        return userService.assignPaymentCustomerIdIfAbsent(userId, paymentCustomerId);
    }

    private com.stripe.model.billingportal.Session createPortalSession(String paymentCustomerId) {
        try {
            return com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(paymentCustomerId)
                            .setReturnUrl(clientProperties.frontendUrl())
                            .build()
            );
        } catch (StripeException exception) {
            throw new PaymentException("Unable to create session for billing portal", exception);
        }
    }
}
