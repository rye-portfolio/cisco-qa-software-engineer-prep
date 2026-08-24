package com.example.webapp.repository;

import com.example.webapp.model.Order;
import com.example.webapp.model.OrderLine;
import com.example.webapp.model.OrderStatus;
import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void savesAndFindsUserByUsername() {
        userRepository.save(new User("alice", "hash", true, false, false));

        Optional<User> found = userRepository.findByUsername("alice");

        assertThat(found).isPresent();
        assertThat(found.get().isManageUsers()).isTrue();
        assertThat(found.get().isManageStock()).isFalse();
    }

    @Test
    void savesOrderWithLinesAndCascadesPersistence() {
        User creator = userRepository.save(new User("bob", "hash", false, false, false));
        StockItem widget = stockItemRepository.save(new StockItem("Widget", 10));

        Order order = new Order(creator, Instant.now(), OrderStatus.COMPLETED);
        order.addLine(new OrderLine(widget, 3));
        Order saved = orderRepository.save(order);

        List<Order> found = orderRepository.findByCreator(creator);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getLines()).hasSize(1);
        assertThat(found.get(0).getLines().get(0).getQuantity()).isEqualTo(3);
        assertThat(saved.getId()).isNotNull();
    }
}
