package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponseDTO(Long id,
                                      Long customerId,
                                      OrderStatus status,
                                      BigDecimal total,
                                      LocalDateTime createdAt) {
}