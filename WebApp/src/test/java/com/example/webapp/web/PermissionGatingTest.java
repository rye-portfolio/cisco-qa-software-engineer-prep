package com.example.webapp.web;

import com.example.webapp.model.StockItem;
import com.example.webapp.model.User;
import com.example.webapp.repository.StockItemRepository;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.OrderRequestLine;
import com.example.webapp.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PermissionGatingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private OrderService orderService;

    @Test
    @WithMockUser(username = "plain")
    void anyLoggedInUserCanViewStock() throws Exception {
        mockMvc.perform(get("/stock")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "plain")
    void creatingStockItemRequiresManageStock() throws Exception {
        mockMvc.perform(post("/stock/create").with(csrf())
                        .param("name", "Thing").param("quantity", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager", authorities = "MANAGE_STOCK")
    void creatingStockItemSucceedsWithManageStock() throws Exception {
        mockMvc.perform(post("/stock/create").with(csrf())
                        .param("name", "Thing").param("quantity", "1"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "plain")
    void usersPageRequiresManageUsers() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "MANAGE_USERS")
    void usersPageAccessibleWithManageUsers() throws Exception {
        mockMvc.perform(get("/users")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "orders-owner-a")
    void userWithoutViewAllOrdersFallsBackToOwnOrdersWhenRequestingAll() throws Exception {
        userRepository.save(new User("orders-owner-a", "hash", false, false, false));
        User other = userRepository.save(new User("orders-owner-b", "hash", false, false, false));
        StockItem item = stockItemRepository.save(new StockItem("Widget-A", 10));
        orderService.createOrder(other, List.of(new OrderRequestLine(item.getId(), 1)));

        mockMvc.perform(get("/orders").param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("viewingAll", false))
                .andExpect(model().attribute("orders", org.hamcrest.Matchers.empty()));
    }

    @Test
    @WithMockUser(username = "orders-viewer", authorities = "VIEW_ALL_ORDERS")
    void userWithViewAllOrdersSeesOtherUsersOrders() throws Exception {
        userRepository.save(new User("orders-viewer", "hash", false, false, true));
        User other = userRepository.save(new User("orders-owner-c", "hash", false, false, false));
        StockItem item = stockItemRepository.save(new StockItem("Widget-B", 10));
        orderService.createOrder(other, List.of(new OrderRequestLine(item.getId(), 1)));

        mockMvc.perform(get("/orders").param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("viewingAll", true))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString("orders-owner-c")));
    }
}
