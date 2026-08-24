package com.example.webapp.security;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AppUserDetailsService service = new AppUserDetailsService(userRepository);

    @Test
    void mapsAllPermissionFlagsToAuthorities() {
        User admin = new User("admin", "hash", true, true, true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("MANAGE_USERS", "MANAGE_STOCK", "VIEW_ALL_ORDERS");
    }

    @Test
    void mapsNoAuthoritiesWhenNoPermissionsGranted() {
        User plain = new User("user", "hash", false, false, false);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(plain));

        UserDetails details = service.loadUserByUsername("user");

        assertThat(details.getAuthorities()).isEmpty();
    }

    @Test
    void throwsWhenUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
