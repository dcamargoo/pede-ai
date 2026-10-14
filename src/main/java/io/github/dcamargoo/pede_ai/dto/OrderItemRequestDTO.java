package io.github.dcamargoo.pede_ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequestDTO(

        @NotNull(message = "O item do cardápio é obrigatório")
        Long menuItemId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantity
) {
}