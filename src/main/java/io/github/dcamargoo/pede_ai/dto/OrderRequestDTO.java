package io.github.dcamargoo.pede_ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(

        @NotNull(message = "O cliente é obrigatório")
        Long customerId,

        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String notes,

        @NotEmpty(message = "O pedido precisa ter pelo menos 1 item")
        List<@Valid OrderItemRequestDTO> items
) {
}