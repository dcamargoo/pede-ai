package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderRequestDTO(

        @Schema(description = "Observações do pedido (opcional)", example = "Sem cebola")
        @Size(max = 255, message = "As observações devem ter no máximo 255 caracteres")
        String notes,

        @Schema(description = "Itens do pedido. Não repita o mesmo item, use a quantidade")
        @NotEmpty(message = "O pedido precisa ter pelo menos 1 item")
        List<@Valid OrderItemRequestDTO> items
) {
}
