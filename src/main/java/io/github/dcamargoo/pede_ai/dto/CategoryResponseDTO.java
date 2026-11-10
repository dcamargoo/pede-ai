package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CategoryResponseDTO(
        @Schema(description = "Id da categoria", example = "1")
        Long id,

        @Schema(description = "Nome da categoria", example = "Lanches")
        String name) {
}
