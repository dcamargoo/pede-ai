package io.github.dcamargoo.pede_ai.controller;

import io.github.dcamargoo.pede_ai.dto.UserRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UserResponseDTO;
import io.github.dcamargoo.pede_ai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRequestDTO userRequestDTO){
        UserResponseDTO response = userService.registerUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id){
        UserResponseDTO response = userService.findUserById(id);
        return ResponseEntity.ok(response);
    }
}
