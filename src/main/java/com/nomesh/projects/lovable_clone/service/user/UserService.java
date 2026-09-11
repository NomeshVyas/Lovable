package com.nomesh.projects.lovable_clone.service.user;

import com.nomesh.projects.lovable_clone.dto.auth.UserProfileResponse;
import com.nomesh.projects.lovable_clone.entity.User;

public interface UserService {

    User getCurrentUser();

    UserProfileResponse getMyProfile();

    String assignPaymentCustomerIdIfAbsent(Long userId, String customerId);
}
