package io.github.dcamargoo.pede_ai.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MenuItemRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String description,

        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
        BigDecimal price,

        @NotNull(message = "A categoria é obrigatória")
        Long categoryId
) {}
