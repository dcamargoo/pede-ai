package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequestDTO(

        @Schema(description = "Id do item do cardápio", example = "1")
        @NotNull(message = "O item do cardápio é obrigatório")
        Long menuItemId,

        @Schema(description = "Quantidade desejada", example = "2")
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantity
) {
}
