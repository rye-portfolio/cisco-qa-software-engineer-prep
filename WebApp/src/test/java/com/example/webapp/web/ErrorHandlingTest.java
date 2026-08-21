package com.example.webapp.web;

import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.OrderRepository;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.FlashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Covers the final-review findings: foreseeable bad input (duplicate username, unknown id,
 * empty order submission) should render a friendly error instead of a whitelabel 500 or a
 * silently-created empty order, and every page should offer a working logout.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @WithMockUser(username = "order-zero-user")
    void submittingOrderWithAllZeroQuantitiesShowsErrorInsteadOfEmptyOrder() throws Exception {
        User creator = userRepository.save(new User("order-zero-user", "hash", false, false, false));
        StockItem item = stockItemRepository.save(new StockItem("Zero-Qty-Widget", 5));
        long ordersBefore = orderRepository.findByCreator(creator).size();

        mockMvc.perform(post("/orders").with(csrf())
                        .param("stockItemId", item.getId().toString())
                        .param("quantity", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error",
                        "Select at least one item and quantity to create an order."));

        assertThat(orderRepository.findByCreator(creator)).hasSize((int) ordersBefore);
    }

    @Test
    @WithMockUser(username = "admin-dup", authorities = "MANAGE_USERS")
    void creatingUserWithDuplicateUsernameDoesNotReturn500() throws Exception {
        userRepository.save(new User("duplicate-name", "hash", false, false, false));

        mockMvc.perform(post("/users/create").with(csrf())
                        .param("username", "duplicate-name")
                        .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    FlashMap flashMap = result.getFlashMap();
                    assertThat(flashMap.get("error")).isNotNull();
                });
    }

    @Test
    @WithMockUser(username = "admin-unknown", authorities = "MANAGE_USERS")
    void updatingUnknownUserIdDoesNotReturn500() throws Exception {
        mockMvc.perform(post("/users/update").with(csrf())
                        .param("id", "999999")
                        .param("manageUsers", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    FlashMap flashMap = result.getFlashMap();
                    assertThat(flashMap.get("error")).isNotNull();
                });
    }

    @Test
    @WithMockUser(username = "stock-manager", authorities = "MANAGE_STOCK")
    void updatingUnknownStockIdDoesNotReturn500() throws Exception {
        mockMvc.perform(post("/stock/update").with(csrf())
                        .param("id", "999999")
                        .param("quantity", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(result -> {
                    FlashMap flashMap = result.getFlashMap();
                    assertThat(flashMap.get("error")).isNotNull();
                });
    }

    @Test
    @WithMockUser(username = "nav-check-plain")
    void stockPageOffersLogout() throws Exception {
        mockMvc.perform(get("/stock"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/logout")));
    }

    @Test
    @WithMockUser(username = "nav-check-admin", authorities = "MANAGE_USERS")
    void usersPageOffersLogout() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/logout")));
    }
}
