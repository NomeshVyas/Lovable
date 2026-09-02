package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    CheckoutResponse toCheckoutResponse(String checkoutUrl);

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PortalResponse toPortalResponse(String portalUrl);
}
