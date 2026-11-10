package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
        @Schema(description = "Id do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "Maria Silva")
        String name,

        @Schema(description = "Email do usuário", example = "maria@email.com")
        String email,

        @Schema(description = "Papel do usuário no sistema", example = "CUSTOMER")
        Role role) {
}
