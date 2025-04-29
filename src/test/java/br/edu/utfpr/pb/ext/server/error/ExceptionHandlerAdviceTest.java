package br.edu.utfpr.pb.ext.server.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class ExceptionHandlerAdviceTest {
  public static final String VALIDATION_ERROR = "Validation Error";
  @Mock private HttpServletRequest request;
  @InjectMocks private ExceptionHandlerAdvice exceptionHandlerAdvice;

  @BeforeEach
  void setUp() {
    when(request.getServletPath()).thenReturn("/url");
  }

  @Test
  @Description("Valida se ao receber um erro, os campos são apresentados corretamente")
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
  @Description(
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
  @Description("Valida se erro é formatado corretamente quando não há validationError")
  void handleMethodArgumentNotValidException_whenNoValidationError_ShouldReturnApiError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).isEmpty();
  }

  @Test
  @Description("Valida se erro é formatado corretamente quando a mensagem tem valor nulo")
  void
      handleMethodArgumentNotValidException_whenValidationErrorMessageIsNull_ShouldReturnApiError() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
    bindingResult.addError(new FieldError("obj", "email", null));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
    ApiError apiError = exceptionHandlerAdvice.handleMethodArgumentNotValidException(ex, request);

    assertThat(apiError.getValidationErrors()).containsEntry("email", null);
  }

  @Test
  @Description("Valida que o último erro ocorrido é carregado em caso de duplicação de erros")
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
}
