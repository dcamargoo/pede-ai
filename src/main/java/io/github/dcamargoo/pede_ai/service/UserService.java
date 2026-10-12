package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.UserRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UserResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
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
        newUser.setPassword(request.password());
        newUser.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(newUser);
        return toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toUserResponse(user);
    }

    private UserResponseDTO toUserResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
