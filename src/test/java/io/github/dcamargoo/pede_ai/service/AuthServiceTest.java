package io.github.dcamargoo.pede_ai.service;

import io.github.dcamargoo.pede_ai.dto.LoginRequestDTO;
import io.github.dcamargoo.pede_ai.dto.LoginResponseDTO;
import io.github.dcamargoo.pede_ai.entity.User;
import io.github.dcamargoo.pede_ai.enums.Role;
import io.github.dcamargoo.pede_ai.exception.UnauthorizedException;
import io.github.dcamargoo.pede_ai.repository.UserRepository;
import io.github.dcamargoo.pede_ai.security.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Marina");
        user.setEmail("marina@email.com");
        user.setPassword("hashed-password");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    @Nested
    class Login {

        @Test
        void shouldReturnToken_whenEmailAndPasswordAreCorrect() {
            User user = buildUser();
            LoginRequestDTO request = new LoginRequestDTO("marina@email.com", "senha123");
            when(userRepository.findByEmail("marina@email.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha123", "hashed-password")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("fake-token");

            LoginResponseDTO response = authService.login(request);

            assertThat(response.token()).isEqualTo("fake-token");
        }

        @Test
        void shouldThrowUnauthorizedException_whenEmailIsNotRegistered() {
            LoginRequestDTO request = new LoginRequestDTO("naoexiste@email.com", "senha123");
            when(userRepository.findByEmail("naoexiste@email.com")).thenReturn(Optional.empty());

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));

            assertThat(exception.getMessage()).isEqualTo("Email ou senha inválidos");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        void shouldThrowUnauthorizedException_whenPasswordIsWrong() {
            User user = buildUser();
            LoginRequestDTO request = new LoginRequestDTO("marina@email.com", "senhaErrada");
            when(userRepository.findByEmail("marina@email.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senhaErrada", "hashed-password")).thenReturn(false);

            UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                    () -> authService.login(request));

            assertThat(exception.getMessage()).isEqualTo("Email ou senha inválidos");
            verify(jwtService, never()).generateToken(any());
        }
    }
}
