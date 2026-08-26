package com.codems.ordertracker.common.security.service;

import com.codems.ordertracker.common.security.model.SecurityUser;
import com.codems.ordertracker.domain.user.entity.User;
import com.codems.ordertracker.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public SecurityUser loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return SecurityUser.from(user);
    }
}
