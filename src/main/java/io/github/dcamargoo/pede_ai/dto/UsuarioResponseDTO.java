package io.github.dcamargoo.pede_ai.dto;

import io.github.dcamargoo.pede_ai.enums.Perfil;

public record UsuarioResponseDTO(Long id, String nome, String email, Perfil perfil) {
}
