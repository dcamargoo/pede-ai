package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MenuItemRequestDTO(

        @Schema(description = "Nome do item", example = "Hambúrguer")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Schema(description = "Descrição do item (opcional)", example = "Pão, carne, queijo e salada")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String description,

        @Schema(description = "Preço do item, com até 2 casas decimais", example = "32.00")
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
        BigDecimal price,

        @Schema(description = "Id da categoria do item", example = "1")
        @NotNull(message = "A categoria é obrigatória")
        Long categoryId
) {}
