package br.edu.utfpr.pb.ext.server.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

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

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleIllegalArgumentException(
          IllegalArgumentException ex, HttpServletRequest request) {
    ApiError apiError =
            ApiError.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(ex.getMessage()) // Usa a mensagem da exceção original
                    .url(request.getServletPath())
                    .build();
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }
}
