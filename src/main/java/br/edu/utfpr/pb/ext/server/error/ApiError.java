package br.edu.utfpr.pb.ext.server.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ApiError {
  private long timestamp = new Date().getTime();
  private int status;
  private String message;
  private String url;
  private Map<String, String> validationErrors;
}
