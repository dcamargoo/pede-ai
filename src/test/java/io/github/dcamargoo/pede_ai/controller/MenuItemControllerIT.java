package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import io.github.dcamargoo.pede_ai.entity.Category;
import io.github.dcamargoo.pede_ai.entity.MenuItem;
import io.github.dcamargoo.pede_ai.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MenuItemControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateMenuItem_whenCategoryExists() throws Exception {
        Category category = createCategory("Lanches");
        String requestBody = """
                {
                  "name": "Hambúrguer",
                  "description": "Pão, carne e queijo",
                  "price": 32.00,
                  "categoryId": %d
                }
                """.formatted(category.getId());

        mockMvc.perform(post("/api/v1/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Hambúrguer"))
                .andExpect(jsonPath("$.price").value(32.0))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.category.name").value("Lanches"));
    }

    @Test
    void shouldListOnlyAvailableItemsOfCategory_whenBothFiltersAreInformed() throws Exception {
        Category snacks = createCategory("Lanches");
        Category drinks = createCategory("Bebidas");
        createMenuItem("Hambúrguer", "32.00", true, snacks);
        createMenuItem("X-Bacon", "38.00", false, snacks);
        createMenuItem("Refrigerante", "7.00", true, drinks);

        mockMvc.perform(get("/api/v1/menu-items")
                        .param("categoryId", snacks.getId().toString())
                        .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Hambúrguer"));
    }

    @Test
    void shouldReturnConflict_whenDeletingItemThatWasAlreadyOrdered() throws Exception {
        User customer = createCustomer("marina@email.com");
        Category category = createCategory("Lanches");
        MenuItem burger = createMenuItem("Hambúrguer", "32.00", true, category);
        createOrder(customer, burger, 1);

        mockMvc.perform(delete("/api/v1/menu-items/" + burger.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflito"));

        assertThat(menuItemRepository.existsById(burger.getId())).isTrue();
    }
}
