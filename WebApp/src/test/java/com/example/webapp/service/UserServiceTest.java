package com.example.webapp.service;

import com.example.webapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUserHashesPassword() {
        User created = userService.createUser("newperson", "s3cret", false, true, false);

        assertThat(created.getPasswordHash()).isNotEqualTo("s3cret");
        assertThat(passwordEncoder.matches("s3cret", created.getPasswordHash())).isTrue();
        assertThat(created.isManageStock()).isTrue();
        assertThat(created.isManageUsers()).isFalse();
    }

    @Test
    void updatePermissionsChangesFlags() {
        User created = userService.createUser("anotherperson", "pw", false, false, false);

        User updated = userService.updatePermissions(created.getId(), true, true, true);

        assertThat(updated.isManageUsers()).isTrue();
        assertThat(updated.isManageStock()).isTrue();
        assertThat(updated.isViewAllOrders()).isTrue();
    }
}
