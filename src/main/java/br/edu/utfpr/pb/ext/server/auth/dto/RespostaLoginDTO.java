
package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(
  name = "RespostaLoginDTO",
  description = "Resposta retornada após um login bem-sucedido no sistema",
  title = "Resposta de Login",
  example = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"expiresIn\":3600}"
)
public class RespostaLoginDTO {

  @Schema(
    description = "Token JWT gerado para autenticação do usuário",
    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    required = true
  )
  private String token;

  @Schema(
    description = "Tempo de expiração do token em segundos",
    example = "3600",
    required = true,
    minimum = "0"
  )
  private long expiresIn;
}
