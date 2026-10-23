package io.github.dcamargoo.pede_ai.exception;

import io.github.dcamargoo.pede_ai.dto.ErrorResponseDTO;
import io.github.dcamargoo.pede_ai.dto.ErrorResponseDTO.FieldErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: recurso não existe
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex,
                                                           HttpServletRequest request) {
        log.warn("Recurso não encontrado em {}: {}", request.getRequestURI(), ex.getMessage());

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
        log.warn("Conflito em {}: {}", request.getRequestURI(), ex.getMessage());

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
        log.warn("Regra de negócio violada em {}: {}", request.getRequestURI(), ex.getMessage());

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

        log.warn("Dados inválidos em {}: {}", request.getRequestURI(), fields);

        return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos estão inválidos", request, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadable(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        log.warn("Corpo da requisição inválido em {}: {}", request.getRequestURI(), ex.getMessage());

        return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "O corpo da requisição está ausente ou malformado", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                               HttpServletRequest request) {
        log.warn("Parâmetro inválido em {}: {}={}", request.getRequestURI(), ex.getName(), ex.getValue());

        return buildResponse(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Valor inválido para o parâmetro '" + ex.getName() + "'", request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResource(NoResourceFoundException ex,
                                                             HttpServletRequest request) {
        log.warn("Rota não encontrada: {} {}", request.getMethod(), request.getRequestURI());

        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                "Rota não encontrada", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        log.warn("Método {} não suportado em {}", ex.getMethod(), request.getRequestURI());

        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido",
                "O método " + ex.getMethod() + " não é suportado nesta rota", request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                        HttpServletRequest request) {
        log.warn("Content-Type não suportado em {}: {}", request.getRequestURI(), ex.getContentType());

        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de mídia não suportado",
                "Envie o corpo com Content-Type: application/json", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Violação de integridade dos dados em {}", request.getRequestURI());

        return buildResponse(HttpStatus.CONFLICT, "Conflito",
                "A operação conflita com dados já existentes", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex,
                                                             HttpServletRequest request) {

        log.error("Erro inesperado em {}", request.getRequestURI(), ex);

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request, null);
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