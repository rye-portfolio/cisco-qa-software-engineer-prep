package com.example.webapp.config;

import com.example.webapp.model.User;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SeedDataRunnerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    private SeedDataRunner runner() {
        UserService userService = new UserService(userRepository, new BCryptPasswordEncoder());
        return new SeedDataRunner(userRepository, userService, stockItemRepository);
    }

    @Test
    void seedsFourUsersWithExpectedPermissionsWhenTableEmpty() throws Exception {
        runner().run();

        assertThat(userRepository.findByUsername("admin").orElseThrow().isManageUsers()).isTrue();
        assertThat(userRepository.findByUsername("stockmanager").orElseThrow().isManageStock()).isTrue();
        assertThat(userRepository.findByUsername("stockmanager").orElseThrow().isManageUsers()).isFalse();
        assertThat(userRepository.findByUsername("orderviewer").orElseThrow().isViewAllOrders()).isTrue();
        assertThat(stockItemRepository.count()).isEqualTo(4);
    }

    @Test
    void doesNotReseedWhenUsersAlreadyExist() throws Exception {
        userRepository.save(new User("existing", "hash", false, false, false));

        runner().run();

        assertThat(userRepository.count()).isEqualTo(1);
    }
}
