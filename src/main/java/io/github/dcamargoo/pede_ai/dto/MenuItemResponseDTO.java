package io.github.dcamargoo.pede_ai.dto;

import java.math.BigDecimal;

public record MenuItemResponseDTO(Long id,
                                   String name,
                                   String description,
                                   BigDecimal price,
                                   boolean available,
                                   CategoryResponseDTO category) {
}
