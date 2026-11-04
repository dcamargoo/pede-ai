package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldReturnToken_whenEmailAndPasswordAreCorrect() throws Exception {
        createCustomer("marina@email.com");
        String requestBody = """
                {
                  "email": "marina@email.com",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        createCustomer("marina@email.com");
        String requestBody = """
                {
                  "email": "marina@email.com",
                  "password": "senhaErrada"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void shouldReturnUnauthorized_whenEmailIsNotRegistered() throws Exception {
        String requestBody = """
                {
                  "email": "naoexiste@email.com",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    void shouldReturnBadRequest_whenEmailIsBlank() throws Exception {
        String requestBody = """
                {
                  "email": "",
                  "password": "senha123"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
