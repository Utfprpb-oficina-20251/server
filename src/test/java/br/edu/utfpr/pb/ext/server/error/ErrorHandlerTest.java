package br.edu.utfpr.pb.ext.server.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

  @Mock private ErrorAttributes errorAttributes;
  @Mock private WebRequest webRequest;
  @InjectMocks private ErrorHandler errorHandler;

  private Map<String, Object> attributes;

  @BeforeEach
  void setUp() {
    attributes = new HashMap<>();
    attributes.put("message", "error message");
    attributes.put("path", "/url");
    attributes.put("status", 400);

    when(errorAttributes.getErrorAttributes(eq(webRequest), any(ErrorAttributeOptions.class)))
        .thenReturn(attributes);
  }

  @Test
  @DisplayName("Erro deve retornar todos os parâmetros quando existentes")
  void handleError_WhenAllParametersExist_ShouldReturnApiErrorWithAttributes() {
    ApiError result = errorHandler.handleError(webRequest);

    assertThat(result.getMessage()).isEqualTo("error message");
    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName(
      "Erro deve retornar parâmetros quando proveniente de método não seguro (POST/PUT/DELETE)")
  void handleUnsafeError_WhenAllParametersExist_ShouldReturnApiErrorWithAttributes() {
    ApiError result = errorHandler.handleUnsafeError(webRequest);
    assertThat(result.getMessage()).isEqualTo("error message");
    assertThat(result.getStatus()).isEqualTo(400);
    assertThat(result.getUrl()).isEqualTo("/url");
  }

  @Test
  @DisplayName("Erro deve retornar o status 500 quando não houver status no atributo")
  void handleError_WhenStatusDoesNotExistOnAttributes_ShouldReturnApiErrorWithDefaultStatusCode() {
    attributes.remove("status");
    ApiError result = errorHandler.handleError(webRequest);
    assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Test
  @DisplayName("Erro deve ser retornado no caso do campo path estar nulo")
  void handleError_whenPathDoesNotExist_ShouldReturnApiErrorWithAttributes() {
    attributes.remove("path");
    ApiError result = errorHandler.handleError(webRequest);
    assertThat(result.getUrl()).isNull();
  }

  @Test
  @DisplayName("Erro deve ser retornado no caso do campo mensagem estar nulo")
  void handleError_WhenMessageNotExist_ShouldReturnApiErrorWithAttributes() {
    attributes.remove("message");
    ApiError result = errorHandler.handleError(webRequest);
    assertThat(result.getMessage()).isNull();
  }
}
