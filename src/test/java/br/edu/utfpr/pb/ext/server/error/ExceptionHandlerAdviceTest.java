package br.edu.utfpr.pb.ext.server.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerAdviceTest {
  public static final String VALIDATION_ERROR = "Validation Error";
  public static final String REQUISICAO_INVALIDA = "Requisição inválida.";

  @Mock private HttpServletRequest request;
  @InjectMocks private ExceptionHandlerAdvice exceptionHandlerAdvice;

  @BeforeEach
  void setUp() {
    when(request.getServletPath()).thenReturn("/url");
  }

  @Test
  @DisplayName("Valida se ao receber um erro, os campos são apresentados corretamente")
  void
      handleMethodArgumentNotValidException_whenMethodArgumentNotValidException_ShouldReturnAllFieldErrors() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "erro", "um erro ocorreu"));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(VALIDATION_ERROR);
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors())
        .containsExactlyInAnyOrderEntriesOf(Map.of("erro", "um erro ocorreu"));
  }

  @Test
  @DisplayName(
      "Valida se ao enviar mais de um erro, esses erros são apresentados no corpo da resposta")
  void
      handleMethodArgumentNotValidException_whenExceptionWithMultipleErrors_ShouldReturnMultipleErrorsMap() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "email", "email com formato inválido"));
    bindingResult.addError(new FieldError("obj", "cpf", "cpf inválido"));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).hasSize(2);
    assertThat(apiError.getValidationErrors())
        .containsExactlyInAnyOrderEntriesOf(
            Map.of("email", "email com formato inválido", "cpf", "cpf inválido"));
  }

  @Test
  @DisplayName("Valida se erro é formatado corretamente quando não há validationError")
  void handleMethodArgumentNotValidException_whenNoValidationError_ShouldReturnApiError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).isEmpty();
  }

  @Test
  @DisplayName("Valida se erro é formatado corretamente quando a mensagem tem valor nulo")
  void
      handleMethodArgumentNotValidException_whenValidationErrorMessageIsNull_ShouldReturnApiError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "email", null));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).containsEntry("email", null);
  }

  @Test
  @DisplayName("Valida que o último erro ocorrido é carregado em caso de duplicação de erros")
  void handleMethodArgumentNotValidException_whenDuplicatedError_ShouldReturnApiError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "email", "Formato de email inválido"));
    bindingResult.addError(new FieldError("obj", "email", "Domínio de Email não é válido"));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).hasSize(1);
    assertThat(apiError.getValidationErrors())
        .containsEntry("email", "Domínio de Email não é válido");
  }

  @Test
  @DisplayName("Valida se EntityNotFoundException é tratada corretamente")
  void handleEntityNotFoundException_whenEntityNotFound_ShouldReturnNotFoundError() {
    String errorMessage = "Entidade não encontrada";
    EntityNotFoundException ex = new EntityNotFoundException(errorMessage);

    ApiError apiError = exceptionHandlerAdvice.handleEntityNotFoundException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(404);
    assertThat(apiError.getMessage()).isEqualTo(errorMessage);
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se EntityNotFoundException com mensagem nula é tratada corretamente")
  void handleEntityNotFoundException_whenEntityNotFoundWithNullMessage_ShouldReturnNotFoundError() {
    EntityNotFoundException ex = new EntityNotFoundException();

    ApiError apiError = exceptionHandlerAdvice.handleEntityNotFoundException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(404);
    assertThat(apiError.getMessage()).isNull();
    assertThat(apiError.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName("Valida se AccessDeniedException é tratada corretamente")
  void handleAccessDeniedException_whenAccessDenied_ShouldReturnForbiddenError() {
    AccessDeniedException ex = new AccessDeniedException("Acesso negado");

    ApiError apiError = exceptionHandlerAdvice.handleAccessDeniedException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(403);
    assertThat(apiError.getMessage())
        .isEqualTo("Acesso negado. Você não tem permissão para executar esta ação.");
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se IllegalArgumentException é tratada corretamente")
  void handleIllegalArgumentException_whenIllegalArgument_ShouldReturnBadRequestError() {
    String errorMessage = "Argumento inválido";
    IllegalArgumentException ex = new IllegalArgumentException(errorMessage);

    ApiError apiError = exceptionHandlerAdvice.handleIllegalArgumentException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(errorMessage);
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se IllegalArgumentException com mensagem nula usa mensagem padrão")
  void
      handleIllegalArgumentException_whenIllegalArgumentWithNullMessage_ShouldReturnDefaultMessage() {
    IllegalArgumentException ex = new IllegalArgumentException();

    ApiError apiError = exceptionHandlerAdvice.handleIllegalArgumentException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(REQUISICAO_INVALIDA);
    assertThat(apiError.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName("Valida se ResponseStatusException é tratada corretamente")
  void
      handleResponseStatusException_whenResponseStatusException_ShouldReturnCorrectStatusAndMessage() {
    String errorMessage = "Recurso não encontrado";
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);

    ResponseEntity<ApiError> response =
        exceptionHandlerAdvice.handleResponseStatusException(ex, request);
    ApiError apiError = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(apiError.getStatus()).isEqualTo(404);
    assertThat(apiError.getMessage()).isEqualTo(errorMessage);
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se ResponseStatusException com mensagem nula usa mensagem padrão")
  void
      handleResponseStatusException_whenResponseStatusExceptionWithNullReason_ShouldReturnDefaultMessage() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST);

    ResponseEntity<ApiError> response =
        exceptionHandlerAdvice.handleResponseStatusException(ex, request);
    ApiError apiError = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(REQUISICAO_INVALIDA);
    assertThat(apiError.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName("Valida se Exception genérica é tratada corretamente")
  void handleException_whenGenericException_ShouldReturnInternalServerError() {
    Exception ex = new RuntimeException("Erro interno");

    ApiError apiError = exceptionHandlerAdvice.handleException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(500);
    assertThat(apiError.getMessage())
        .isEqualTo(ex.getMessage());
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se Exception com mensagem nula é tratada corretamente")
  void handleException_whenGenericExceptionWithNullMessage_ShouldReturnInternalServerError() {
    Exception ex = new RuntimeException();

    ApiError apiError = exceptionHandlerAdvice.handleException(ex, request);

    assertThat(apiError.getStatus()).isEqualTo(500);
    assertThat(apiError.getMessage())
        .isEqualTo("Erro interno do servidor. Por favor, tente novamente mais tarde.");
    assertThat(apiError.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName("Valida se HttpMessageNotReadableException é tratada corretamente")
  void handleHttpMessageNotReadableException_whenException_ShouldReturnBadRequestError() {
    String errorMessage = "Mensagem não legível";
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException(errorMessage, mock(HttpInputMessage.class));

    ResponseEntity<ApiError> response =
        exceptionHandlerAdvice.handleHttpMessageNotReadableException(ex, request);
    ApiError apiError = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(errorMessage);
    assertThat(apiError.getUrl()).isEqualTo("/url");
    assertThat(apiError.getValidationErrors()).isNull();
  }

  @Test
  @DisplayName("Valida se HttpMessageNotReadableException com mensagem nula usa mensagem padrão")
  void handleHttpMessageNotReadableException_whenNullMessage_ShouldReturnDefaultMessage() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException(null, mock(HttpInputMessage.class));

    ResponseEntity<ApiError> response =
        exceptionHandlerAdvice.handleHttpMessageNotReadableException(ex, request);
    ApiError apiError = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(apiError.getStatus()).isEqualTo(400);
    assertThat(apiError.getMessage()).isEqualTo(REQUISICAO_INVALIDA);
    assertThat(apiError.getUrl()).isEqualTo("/url");
  }
}
