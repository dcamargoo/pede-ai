package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.Role;

public record UserResponseDTO(Long id, String name, String email, Role role) {
}
