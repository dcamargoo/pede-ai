package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleAuthorizationIT extends AbstractIntegrationTest {

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbidden_whenCustomerTriesToCreateCategory() throws Exception {
        String requestBody = """
                {
                  "name": "Lanches"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        assertThat(categoryRepository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbidden_whenCustomerTriesToAdvanceOrder() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/advance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbidden_whenCustomerTriesToListAllOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbidden_whenCustomerTriesToSeeAnotherUserById() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT")
    void shouldReturnForbidden_whenRestaurantTriesToCreateOrder() throws Exception {
        String requestBody = """
                {
                  "items": [
                    { "menuItemId": 1, "quantity": 1 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RESTAURANT")
    void shouldReturnForbidden_whenRestaurantTriesToCancelOrder() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isForbidden());
    }
}
