package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @Schema(description = "Email do usuário", example = "maria@email.com")
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Schema(description = "Senha do usuário", example = "senha12345")
        @NotBlank(message = "A senha é obrigatória")
        String password
) {}
