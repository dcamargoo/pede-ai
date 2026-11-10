package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponseDTO(
                                      @Schema(description = "Id do pedido", example = "1")
                                      Long id,

                                      @Schema(description = "Id do cliente que fez o pedido", example = "2")
                                      Long customerId,

                                      @Schema(description = "Status atual do pedido", example = "RECEIVED")
                                      OrderStatus status,

                                      @Schema(description = "Valor total do pedido", example = "71.00")
                                      BigDecimal total,

                                      @Schema(description = "Data e hora de criação")
                                      LocalDateTime createdAt) {
}
