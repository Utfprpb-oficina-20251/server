package br.edu.utfpr.pb.ext.server.error;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
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

  /**
   * Inicializa o controlador de erros com o ErrorAttributes fornecido.
   *
   * @param errorAttributes fonte dos detalhes do erro extraídos das requisições web
   */
  public ErrorHandler(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  /**
   * Retorna um objeto {@link ApiError} contendo detalhes do erro ocorrido em requisições HTTP sem efeito colateral, como GET.
   *
   * @param webRequest contexto da requisição web atual
   * @return objeto {@link ApiError} com informações sobre o erro, incluindo mensagem, URL e status HTTP
   */
  @GetMapping("/error")
  @Operation(summary = "Retorna o erro proveniente de métodos HTTP sem efeito colateral")
  public ApiError handleError(WebRequest webRequest) {
    return buildApiError(webRequest);
  }

  /**
   * Retorna um objeto de erro formatado para requisições HTTP que podem causar efeitos colaterais, como POST, PUT ou DELETE.
   *
   * @param webRequest a requisição web atual
   * @return um {@link ApiError} com detalhes do erro ocorrido na requisição
   */
  @PostMapping("/error")
  @Operation(summary = "Retorna o erro proveniente de métodos HTTP com efeito colateral")
  public ApiError handleUnsafeError(WebRequest webRequest) {
    return buildApiError(webRequest);
  }

  /**
   * Constrói um objeto ApiError a partir dos atributos de erro extraídos do WebRequest.
   *
   * Extrai mensagem, caminho da requisição e status HTTP dos atributos de erro, utilizando 500 caso o status não esteja presente.
   *
   * @param webRequest contexto da requisição web contendo os detalhes do erro
   * @return uma instância de ApiError representando o erro ocorrido
   */
  private ApiError buildApiError(WebRequest webRequest) {
    Map<String, Object> attributes =
        errorAttributes.getErrorAttributes(
            webRequest,
            ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.PATH,
                ErrorAttributeOptions.Include.STATUS));
    return ApiError.builder()
        .message((String) attributes.get("message"))
        .url((String) attributes.get("path"))
        .status(
            attributes.get("status") != null
                ? (Integer) attributes.get("status")
                : HttpStatus.INTERNAL_SERVER_ERROR.value())
        .build();
  }
}
