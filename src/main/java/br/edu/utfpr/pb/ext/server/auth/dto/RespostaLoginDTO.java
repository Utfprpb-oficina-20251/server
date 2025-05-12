package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "RespostaLoginDTO", description = "Resposta de login")
public class RespostaLoginDTO {
  private String token;
  private long expiresIn;
}
