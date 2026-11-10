package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.UserRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UserResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Cadastro e consulta de usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Cadastra um novo usuário")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO userRequestDTO){
        UserResponseDTO response = userService.registerUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Devolve os dados do usuário logado")
    @ApiResponse(responseCode = "200", description = "Dados do usuário")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal User user){
        UserResponseDTO response = userService.findUserById(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Busca um usuário pelo id")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    @ApiResponse(responseCode = "403", description = "Apenas o restaurante pode consultar outros usuários")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id){
        UserResponseDTO response = userService.findUserById(id);
        return ResponseEntity.ok(response);
    }
}
