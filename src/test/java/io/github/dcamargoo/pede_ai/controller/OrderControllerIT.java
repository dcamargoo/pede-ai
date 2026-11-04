package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.entity.Order;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateOrderForLoggedCustomer_whenRequestIsValid() throws Exception {
        User customer = createCustomer("marina@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        MenuItem soda = createMenuItem("Refrigerante", "7.00", true, category);
        String requestBody = """
                {
                  "notes": "Sem cebola",
                  "items": [
                    { "menuItemId": %d, "quantity": 2 },
                    { "menuItemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(burger.getId(), soda.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearerToken(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customer.getId()))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.total").value(71.0))
                .andExpect(jsonPath("$.notes").value("Sem cebola"))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Hambúrguer"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(32.0))
                .andExpect(jsonPath("$.items[0].subtotal").value(64.0));

        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnBadRequest_whenOrderHasNoItems() throws Exception {
        User customer = createCustomer("marina@email.com");
        String requestBody = """
                {
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearerToken(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Dados inválidos"))
                .andExpect(jsonPath("$.fields[0].field").value("items"))
                .andExpect(jsonPath("$.fields[0].message").value("O pedido precisa ter pelo menos 1 item"));
    }

    @Test
    void shouldReturnUnprocessableEntity_whenMenuItemIsUnavailable() throws Exception {
        User customer = createCustomer("marina@email.com");
        Category category = createCategory("Bebidas");
        MenuItem soda = createMenuItem("Refrigerante", "7.00", false, category);
        String requestBody = """
                {
                  "items": [
                    { "menuItemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(soda.getId());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", bearerToken(customer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Regra de negócio violada"))
                .andExpect(jsonPath("$.message").value("O item 'Refrigerante' está indisponível"));

        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void shouldAdvanceOrderToInPreparation_whenOrderIsReceived() throws Exception {
        User restaurant = createRestaurant("restaurante@email.com");
        User customer = createCustomer("marina@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        Order order = createOrder(customer, burger, 1);

        mockMvc.perform(patch("/api/v1/orders/" + order.getId() + "/advance")
                        .header("Authorization", bearerToken(restaurant)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));

        Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }

    @Test
    void shouldReturnUnprocessableEntity_whenCancellingOrderThatIsNotReceived() throws Exception {
        User customer = createCustomer("marina@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        Order order = createOrder(customer, burger, 1);
        order.setStatus(OrderStatus.IN_PREPARATION);
        orderRepository.save(order);

        mockMvc.perform(patch("/api/v1/orders/" + order.getId() + "/cancel")
                        .header("Authorization", bearerToken(customer)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Regra de negócio violada"));

        Order unchangedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.IN_PREPARATION);
    }

    @Test
    void shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        User customer = createCustomer("marina@email.com");

        mockMvc.perform(get("/api/v1/orders/99")
                        .header("Authorization", bearerToken(customer)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.message").value("Pedido não encontrado"))
                .andExpect(jsonPath("$.fields").doesNotExist());
    }

    @Test
    void shouldReturnNotFound_whenCustomerTriesToSeeAnotherCustomersOrder() throws Exception {
        User marina = createCustomer("marina@email.com");
        User joao = createCustomer("joao@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        Order marinasOrder = createOrder(marina, burger, 1);

        mockMvc.perform(get("/api/v1/orders/" + marinasOrder.getId())
                        .header("Authorization", bearerToken(joao)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pedido não encontrado"));
    }

    @Test
    void shouldReturnNotFound_whenCustomerTriesToCancelAnotherCustomersOrder() throws Exception {
        User marina = createCustomer("marina@email.com");
        User joao = createCustomer("joao@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        Order marinasOrder = createOrder(marina, burger, 1);

        mockMvc.perform(patch("/api/v1/orders/" + marinasOrder.getId() + "/cancel")
                        .header("Authorization", bearerToken(joao)))
                .andExpect(status().isNotFound());

        Order unchangedOrder = orderRepository.findById(marinasOrder.getId()).orElseThrow();
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }

    @Test
    void shouldListOnlyOrdersOfLoggedCustomer_whenCallingMyOrders() throws Exception {
        User marina = createCustomer("marina@email.com");
        User joao = createCustomer("joao@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        createOrder(marina, burger, 1);
        createOrder(joao, burger, 2);

        mockMvc.perform(get("/api/v1/orders/me")
                        .header("Authorization", bearerToken(marina)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value(marina.getId()));
    }
}
