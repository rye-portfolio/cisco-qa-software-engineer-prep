package com.example.webapp.service;

import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    private User creator;
    private StockItem widget;
    private StockItem gadget;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(new User("creator", "hash", false, false, false));
        widget = stockItemRepository.save(new StockItem("Widget", 5));
        gadget = stockItemRepository.save(new StockItem("Gadget", 2));
    }

    @Test
    void decrementsStockOnSuccessfulOrder() {
        orderService.createOrder(creator, List.of(new OrderRequestLine(widget.getId(), 3)));

        StockItem updated = stockItemRepository.findById(widget.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(2);
    }

    @Test
    void failsWithoutPartialDecrementWhenAnyLineHasInsufficientStock() {
        assertThatThrownBy(() -> orderService.createOrder(creator, List.of(
                new OrderRequestLine(widget.getId(), 3),
                new OrderRequestLine(gadget.getId(), 10)
        ))).isInstanceOf(InsufficientStockException.class);

        StockItem widgetAfter = stockItemRepository.findById(widget.getId()).orElseThrow();
        StockItem gadgetAfter = stockItemRepository.findById(gadget.getId()).orElseThrow();
        assertThat(widgetAfter.getQuantity()).isEqualTo(5);
        assertThat(gadgetAfter.getQuantity()).isEqualTo(2);
    }
}
