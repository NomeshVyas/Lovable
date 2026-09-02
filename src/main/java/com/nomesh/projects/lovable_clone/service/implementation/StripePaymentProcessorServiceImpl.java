package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.config.ClientProperties;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.exception.PaymentException;
import com.nomesh.projects.lovable_clone.mapper.SubscriptionMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.PaymentEventHandlerService;
import com.nomesh.projects.lovable_clone.service.PaymentProcessorService;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Price;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData;
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
public class StripePaymentProcessorServiceImpl implements PaymentProcessorService {

    PlanRepository planRepository;
    AuthUtil authUtil;
    SubscriptionMapper subscriptionMapper;
    UserRepository userRepository;
    ClientProperties clientProperties;
    SubscriptionService subscriptionService;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest) {
        Plan plan = planRepository.getByIdOrThrow(checkoutRequest.planId());
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.getByIdOrThrow(userId);

        SubscriptionData subscriptionData = SubscriptionData.builder()
                .setBillingMode(
                        SubscriptionData.BillingMode.builder()
                                .setType(SubscriptionData.BillingMode.Type.FLEXIBLE)
                                .build()
                )
                .build();

        String paymentCustomerId = user.getPaymentCustomerId();
        try {
            if (paymentCustomerId == null) {
                Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(user.getEmail())
                            .setName(user.getName())
                            .putMetadata("user_id", userId.toString())
                            .build()
                );
                paymentCustomerId = customer.getId();
                user.setPaymentCustomerId(paymentCustomerId);
                userRepository.save(user);
            }

            SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
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
                    .putMetadata("user_id", userId.toString())
                    .setCustomer(paymentCustomerId)
                    .build();
                Session session = Session.create(sessionCreateParams);
                return subscriptionMapper.toCheckoutResponse(session.getUrl());
        } catch (StripeException exception) {
            throw new PaymentException("Unable to create Stripe checkout session");
        }
    }

    @Override
    public PortalResponse openCustomerPortal() {
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.getByIdOrThrow(userId);
        String paymentCustomerId = user.getPaymentCustomerId();

        if (paymentCustomerId == null || paymentCustomerId.isEmpty())
            throw new BadRequestException("User does not have a Stripe Customer Id, userId: " + userId);

        String portalSessionUrl = createPortalSession(paymentCustomerId).getUrl();
        return subscriptionMapper.toPortalResponse(portalSessionUrl);
    }

    private com.stripe.model.billingportal.Session createPortalSession(String paymentCustomerId) {
        try {
            return com.stripe.model.billingportal.Session.create(
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(paymentCustomerId)
                            .setReturnUrl(clientProperties.frontendUrl())
                            .build()
            );
        } catch (StripeException e) {
            log.error("Failed to create billing portal session for customer {}", paymentCustomerId, e);
            throw new PaymentException("Unable to create session for billing portal");
        }
    }
}
