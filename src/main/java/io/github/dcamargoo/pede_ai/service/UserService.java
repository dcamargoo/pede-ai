package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.UserRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UserResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO request) {
        boolean exists = userRepository.existsByEmail(request.email());
        if (exists) {
            throw new ConflictException("Email já cadastrado");
        }

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.CUSTOMER);

        User savedUser = userRepository.save(newUser);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toResponse(user);
    }

    private UserResponseDTO toResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
