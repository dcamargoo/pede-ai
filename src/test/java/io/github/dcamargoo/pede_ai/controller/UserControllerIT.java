package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import io.github.dcamargoo.pede_ai.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void shouldReturnLoggedUser_whenCallingMe() throws Exception {
        User customer = createCustomer("marina@email.com");

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearerToken(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.email").value("marina@email.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
