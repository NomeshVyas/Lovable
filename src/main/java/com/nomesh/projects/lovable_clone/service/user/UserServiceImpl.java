package com.nomesh.projects.lovable_clone.service.user;

import com.nomesh.projects.lovable_clone.dto.auth.UserProfileResponse;
import com.nomesh.projects.lovable_clone.entity.User;
import com.nomesh.projects.lovable_clone.mapper.UserMapper;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    UserRepository userRepository;
    AuthUtil authUtil;
    UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        return userMapper.toUserProfileResponse(getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Long userId = authUtil.getCurrentUserId();
        return userRepository.getByIdOrThrow(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElseThrow(
                () -> new UsernameNotFoundException("Username not found for: " + identifier)
        );
    }

    @Override
    @Transactional
    public String assignPaymentCustomerIdIfAbsent(Long userId, String customerId) {
        User user = userRepository.getByIdOrThrow(userId);
        if (user.getPaymentCustomerId() == null) {
            user.setPaymentCustomerId(customerId);
            userRepository.save(user);
        }

        return user.getPaymentCustomerId();
    }
}
