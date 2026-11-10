package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
                                   @Schema(description = "Id do item do cardápio", example = "1")
                                   Long menuItemId,

                                   @Schema(description = "Nome do item no momento do pedido", example = "Hambúrguer")
                                   String name,

                                   @Schema(description = "Quantidade pedida", example = "2")
                                   Integer quantity,

                                   @Schema(description = "Preço unitário no momento do pedido", example = "32.00")
                                   BigDecimal unitPrice,

                                   @Schema(description = "Preço unitário multiplicado pela quantidade", example = "64.00")
                                   BigDecimal subtotal) {
}
