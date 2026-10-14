package io.github.dcamargoo.pede_ai.dto;

import java.math.BigDecimal;

public record OrderItemResponseDTO(Long menuItemId,
                                   String name,
                                   Integer quantity,
                                   BigDecimal unitPrice,
                                   BigDecimal subtotal) {
}
