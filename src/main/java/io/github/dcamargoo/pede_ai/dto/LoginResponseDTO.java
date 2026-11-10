package io.github.dcamargoo.pede_ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponseDTO(
        @Schema(description = "Token JWT para enviar no header Authorization como Bearer")
        String token) {}
