package com.example.webapp.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PermissionGatingTest {

    @Autowired
    private MockMvc mockMvc;

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
}
