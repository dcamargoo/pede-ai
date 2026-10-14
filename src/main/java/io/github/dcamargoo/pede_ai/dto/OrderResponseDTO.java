package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(Long id,
                               Long customerId,
                               OrderStatus status,
                               BigDecimal total,
                               String notes,
                               LocalDateTime createdAt,
                               LocalDateTime updatedAt,
                               List<OrderItemResponseDTO> items) {
}
