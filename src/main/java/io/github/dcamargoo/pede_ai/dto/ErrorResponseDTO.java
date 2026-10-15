package io.github.dcamargoo.pede_ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldErrorDTO> fields
) {
    public record FieldErrorDTO(String field, String message) {}
}
