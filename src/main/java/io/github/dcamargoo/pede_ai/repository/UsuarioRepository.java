package io.github.dcamargoo.pede_ai.repository;

import io.github.dcamargoo.pede_ai.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
