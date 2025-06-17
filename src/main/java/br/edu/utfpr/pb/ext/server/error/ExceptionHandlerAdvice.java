package br.edu.utfpr.pb.ext.server.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
  public static final Logger logger =
      org.slf4j.LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
  public static final String REQUISICAO_INVALIDA = "Requisição inválida.";

  @ExceptionHandler({MethodArgumentNotValidException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleMethodArgumentNotValidException(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    Map<String, String> validationErrors = new HashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return ApiError.builder()
        .status(400)
        .message("Validation Error")
        .url(request.getServletPath())
        .validationErrors(validationErrors)
        .build();
  }

  @ExceptionHandler({EntityNotFoundException.class})
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError handleEntityNotFoundException(
      EntityNotFoundException exception, HttpServletRequest request) {

    return ApiError.builder()
        .status(404)
        .message(exception.getMessage())
        .url(request.getServletPath())
        .build();
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN) // Define o status da resposta para 403 Forbidden
  public ApiError handleAccessDeniedException(
      AccessDeniedException exception, HttpServletRequest request) {

    return ApiError.builder()
        .status(403)
        .message("Acesso negado. Você não tem permissão para executar esta ação.")
        .url(request.getServletPath())
        .build();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError handleIllegalArgumentException(
      IllegalArgumentException exception, HttpServletRequest request) {
    return ApiError.builder()
        .status(400)
        .message(exception.getMessage() != null ? exception.getMessage() : REQUISICAO_INVALIDA)
        .url(request.getServletPath())
        .build();
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiError> handleResponseStatusException(
      ResponseStatusException exception, HttpServletRequest request) {
    ApiError apiError =
        ApiError.builder()
            .status(exception.getStatusCode().value())
            .message(exception.getReason() != null ? exception.getReason() : REQUISICAO_INVALIDA)
            .url(request.getServletPath())
            .build();
    return ResponseEntity.status(exception.getStatusCode()).body(apiError);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError handleException(Exception exception, HttpServletRequest request) {
    logger.error("Erro interno do servidor", exception);
    return ApiError.builder()
        .status(500)
        .message("Erro interno do servidor. Por favor, tente novamente mais tarde.")
        .url(request.getServletPath())
        .build();
  }
}
