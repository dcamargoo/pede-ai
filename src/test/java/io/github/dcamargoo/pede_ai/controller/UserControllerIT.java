package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldRegisterCustomerWithoutReturningPassword_whenRequestIsValid() throws Exception {
        String requestBody = """
                {
                  "name": "Marina",
                  "email": "marina@email.com",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Marina"))
                .andExpect(jsonPath("$.email").value("marina@email.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnConflict_whenEmailIsAlreadyRegistered() throws Exception {
        createCustomer("marina@email.com");
        String requestBody = """
                {
                  "name": "Outra Marina",
                  "email": "marina@email.com",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));

        assertThat(userRepository.count()).isEqualTo(1);
    }
}
