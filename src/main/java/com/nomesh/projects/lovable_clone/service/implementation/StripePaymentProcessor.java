package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.config.ClientProperties;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.exception.PaymentException;
import com.nomesh.projects.lovable_clone.exception.ResourceNotFoundException;
import com.nomesh.projects.lovable_clone.mapper.SubscriptionMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.SubscriptionData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StripePaymentProcessor implements PaymentProcessor {

    PlanRepository planRepository;
    AuthUtil authUtil;
    SubscriptionMapper subscriptionMapper;
    UserRepository userRepository;
    ClientProperties clientProperties;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest checkoutRequest) {
        Plan plan = planRepository.findById(checkoutRequest.planId()).orElseThrow(() ->
            new ResourceNotFoundException("Plan", checkoutRequest.planId())
        );
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User", userId)
        );

        SubscriptionData subscriptionData = SubscriptionData.builder()
                .setBillingMode(
                        SubscriptionData.BillingMode.builder()
                                .setType(SubscriptionData.BillingMode.Type.FLEXIBLE)
                                .build()
                )
                .build();

        String customerId = user.getPaymentCustomerId();
        try {
            if (customerId == null) {
                Customer customer = Customer.create(
                    CustomerCreateParams.builder()
                            .setEmail(user.getEmail())
                            .setName(user.getName())
                            .putMetadata("user_id", userId.toString())
                            .build()
                );
                customerId = customer.getId();
                user.setPaymentCustomerId(customerId);
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
                    .setCustomer(customerId)
                    .build();
                Session session = Session.create(sessionCreateParams);
                return subscriptionMapper.toCheckoutResponse(session.getUrl());
        } catch (StripeException exception) {
            throw new PaymentException("Unable to create Stripe checkout session ");
        }
    }

    @Override
    public PortalResponse openCustomerPortal(Long userId) {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("type");
    }
}
