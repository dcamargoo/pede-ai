package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.UserRequestDTO;
import io.github.dcamargoo.pede_ai.dto.UserResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.exception.ConflictException;
import io.github.dcamargoo.pede_ai.exception.ResourceNotFoundException;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Marina");
        user.setEmail("marina@email.com");
        user.setPassword("senha123");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    @Nested
    class RegisterUser {

        @Test
        void shouldRegisterUserAsCustomer_whenEmailIsNotRegistered() {
            UserRequestDTO request = new UserRequestDTO("Marina", "marina@email.com", "senha123");
            when(userRepository.existsByEmail("marina@email.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponseDTO response = userService.registerUser(request);

            assertThat(response.name()).isEqualTo("Marina");
            assertThat(response.email()).isEqualTo("marina@email.com");
            assertThat(response.role()).isEqualTo(Role.CUSTOMER);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        void shouldThrowConflictException_whenEmailIsAlreadyRegistered() {
            UserRequestDTO request = new UserRequestDTO("Marina", "marina@email.com", "senha123");
            when(userRepository.existsByEmail("marina@email.com")).thenReturn(true);

            ConflictException exception = assertThrows(ConflictException.class,
                    () -> userService.registerUser(request));

            assertThat(exception.getMessage()).isEqualTo("Email já cadastrado");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class FindUserById {

        @Test
        void shouldReturnUser_whenUserExists() {
            User user = buildUser();
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            UserResponseDTO response = userService.findUserById(1L);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("Marina");
            assertThat(response.email()).isEqualTo("marina@email.com");
            assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(99L));
        }
    }
}
