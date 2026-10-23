package io.github.dcamargoo.pede_ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
