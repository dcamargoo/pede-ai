package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.UsuarioRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UsuarioResponseDTO;
import io.github.dcamargoo.pede_ai.entity.Usuario;
import io.github.dcamargoo.pede_ai.enums.Perfil;
import io.github.dcamargoo.pede_ai.exception.ConflitoException;
import io.github.dcamargoo.pede_ai.exception.RecursoNaoEncontradoException;
import io.github.dcamargoo.pede_ai.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO request) {
        boolean existe = usuarioRepository.existsByEmail(request.email());
        if (existe) {
            throw new ConflitoException("Email já cadastrado");
        }
        Usuario usuarioNovo = new Usuario();
        usuarioNovo.setNome(request.nome());
        usuarioNovo.setEmail(request.email());
        usuarioNovo.setSenha(request.senha());
        usuarioNovo.setPerfil(Perfil.CLIENTE);
        Usuario usuarioSalvo = usuarioRepository.save(usuarioNovo);
        return toResponse(usuarioSalvo);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
        return toResponse(usuario);
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil()
        );
    }
}
