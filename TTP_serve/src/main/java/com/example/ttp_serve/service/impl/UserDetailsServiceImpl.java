package com.example.ttp_serve.service.impl;

import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(user.getStatus() == UserStatus.INACTIVE)
                .credentialsExpired(false)
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }

    private String[] getAuthorities(User user) {
        switch (user.getUserType()) {
            case SUPER_ADMIN:
                return new String[]{"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_USER"};
            case CAMPUS_ADMIN:
                return new String[]{"ROLE_CAMPUS_ADMIN", "ROLE_ADMIN", "ROLE_USER"};
            case COACH:
                return new String[]{"ROLE_COACH", "ROLE_USER"};
            case STUDENT:
                return new String[]{"ROLE_STUDENT", "ROLE_USER"};
            default:
                return new String[]{"ROLE_USER"};
        }
    }
}