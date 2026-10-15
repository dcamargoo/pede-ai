package io.github.dcamargoo.pede_ai.exception;

import io.github.dcamargoo.pede_ai.dto.ErrorResponseDTO;
import io.github.dcamargoo.pede_ai.dto.ErrorResponseDTO.FieldErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: recurso não existe
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex,
                                                           HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                ex.getMessage(),
                request,
                null);
    }

    // 409: conflito com dados que já existem
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(ConflictException ex,
                                                           HttpServletRequest request) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflito",
                ex.getMessage(),
                request,
                null);
    }

    // 422: regra de negócio violada
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(BusinessRuleException ex,
                                                               HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY,
                "Regra de negócio violada",
                ex.getMessage(),
                request,
                null);
    }

    // 400: falha nas validações do @Valid (@NotBlank, @Positive, @Size...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        List<FieldErrorDTO> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDTO(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos", request, fields);
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status,
                                                           String error,
                                                           String message,
                                                           HttpServletRequest request,
                                                           List<FieldErrorDTO> fields) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                status.value(),
                error,
                message,
                request.getRequestURI(),
                LocalDateTime.now(),
                fields
        );
        return ResponseEntity.status(status).body(body);
    }
}