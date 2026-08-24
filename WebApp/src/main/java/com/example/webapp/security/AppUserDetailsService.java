package com.example.webapp.security;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.isManageUsers()) {
            authorities.add(new SimpleGrantedAuthority("MANAGE_USERS"));
        }
        if (user.isManageStock()) {
            authorities.add(new SimpleGrantedAuthority("MANAGE_STOCK"));
        }
        if (user.isViewAllOrders()) {
            authorities.add(new SimpleGrantedAuthority("VIEW_ALL_ORDERS"));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
