package br.edu.utfpr.pb.ext.server.error;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

/*
 * Classe responsável por gerar formatação de erro para erros provenientes do servlet
 */
@Tag(
    name = "Error",
    description =
        "Endpoint responsável por formatar erro proveniente do servlet, não utilizado programaticamente")
@RestController
public class ErrorHandler implements ErrorController {

  private final ErrorAttributes errorAttributes;

  public ErrorHandler(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  @GetMapping("/error")
  @Operation(summary = "Retorna o erro proveniente de métodos HTTP sem efeito colateral")
  public ApiError handleError(WebRequest webRequest) {
    return buildApiError(webRequest);
  }

  @PostMapping("/error")
  @Operation(summary = "Retorna o erro proveniente de métodos HTTP com efeito colateral")
  public ApiError handleUnsafeError(WebRequest webRequest) {
    return buildApiError(webRequest);
  }

  private ApiError buildApiError(WebRequest webRequest) {
    Map<String, Object> attributes =
        errorAttributes.getErrorAttributes(
            webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
    return ApiError.builder()
        .message((String) attributes.get("message"))
        .url((String) attributes.get("path"))
        .status((Integer) attributes.get("status"))
        .build();
  }
}
