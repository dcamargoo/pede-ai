package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record MenuItemResponseDTO(
                                   @Schema(description = "Id do item", example = "1")
                                   Long id,

                                   @Schema(description = "Nome do item", example = "Hambúrguer")
                                   String name,

                                   @Schema(description = "Descrição do item", example = "Pão, carne, queijo e salada")
                                   String description,

                                   @Schema(description = "Preço do item", example = "32.00")
                                   BigDecimal price,

                                   @Schema(description = "Indica se o item está disponível para pedidos", example = "true")
                                   boolean available,

                                   @Schema(description = "Categoria do item")
                                   CategoryResponseDTO category) {
}
