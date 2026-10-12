package io.github.dcamargoo.pede_ai.dto;

import jakarta.validation.constraints.NotNull;

public record MenuItemAvailabilityRequestDTO(

        @NotNull(message = "Informe se o item está disponível")
        Boolean available
) {}
