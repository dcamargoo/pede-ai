package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginRateLimitIT extends AbstractIntegrationTest {

    private MockHttpServletRequestBuilder loginFrom(String ip) {
        String requestBody = """
                {
                  "email": "marina@email.com",
                  "password": "senhaErrada"
                }
                """;

        return post("/api/v1/auth/login")
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                })
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody);
    }

    @Test
    void shouldReturnTooManyRequests_whenIpExceedsFiveLoginAttempts() throws Exception {
        createCustomer("marina@email.com");

        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(loginFrom("10.0.0.1"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(loginFrom("10.0.0.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.message").value("Muitas tentativas de login. Tente novamente em 1 minuto."));
    }

    @Test
    void shouldCountAttemptsSeparately_forEachIp() throws Exception {
        createCustomer("marina@email.com");

        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(loginFrom("10.0.0.2"));
        }

        mockMvc.perform(loginFrom("10.0.0.3"))
                .andExpect(status().isUnauthorized());
    }
}
