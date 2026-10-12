package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.UsuarioRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UsuarioResponseDTO;
import io.github.dcamargoo.pede_ai.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO usuarioRequestDTO){
        UsuarioResponseDTO response = usuarioService.cadastrarUsuario(usuarioRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id){
        UsuarioResponseDTO response = usuarioService.buscarUsuarioPorId(id);
        return ResponseEntity.ok(response);
    }
}
