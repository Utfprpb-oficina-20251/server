package br.edu.utfpr.pb.ext.server.error;

import java.util.Map;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class ErrorHandler implements ErrorController {

  private final ErrorAttributes errorAttributes;

  public ErrorHandler(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  @GetMapping("/error")
  public ApiError handleError(WebRequest webRequest) {
    return buildApiError(webRequest);
  }

  @PostMapping("/error")
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
