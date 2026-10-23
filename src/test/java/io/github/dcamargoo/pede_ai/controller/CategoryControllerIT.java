package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import io.github.dcamargoo.pede_ai.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldCreateCategory_whenNameIsValid() throws Exception {
        String requestBody = """
                {
                  "name": "Lanches"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Lanches"));

        assertThat(categoryRepository.existsByName("Lanches")).isTrue();
    }

    @Test
    void shouldReturnConflict_whenNameIsAlreadyInUse() throws Exception {
        createCategory("Lanches");
        String requestBody = """
                {
                  "name": "Lanches"
                }
                """;

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflito"))
                .andExpect(jsonPath("$.message").value("Essa categoria já existe"));

        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldUpdateCategory_whenKeepingTheSameName() throws Exception {
        Category category = createCategory("Lanches");
        String requestBody = """
                {
                  "name": "Lanches"
                }
                """;

        mockMvc.perform(put("/api/v1/categories/" + category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Lanches"));
    }

    @Test
    void shouldDeleteCategory_whenCategoryHasNoItems() throws Exception {
        Category category = createCategory("Lanches");

        mockMvc.perform(delete("/api/v1/categories/" + category.getId()))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.existsById(category.getId())).isFalse();
    }

    @Test
    void shouldReturnConflict_whenDeletingCategoryWithItems() throws Exception {
        Category category = createCategory("Lanches");
        createMenuItem("Hambúrguer", "32.00", true, category);

        mockMvc.perform(delete("/api/v1/categories/" + category.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Não é possível remover uma categoria que possui itens"));

        assertThat(categoryRepository.existsById(category.getId())).isTrue();
    }
}
