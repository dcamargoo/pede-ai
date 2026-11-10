package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MenuItemAvailabilityRequestDTO(

        @Schema(description = "Indica se o item está disponível para pedidos", example = "false")
        @NotNull(message = "Informe se o item está disponível")
        Boolean available
) {}
