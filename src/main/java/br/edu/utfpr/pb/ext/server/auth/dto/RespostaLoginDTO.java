package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
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
