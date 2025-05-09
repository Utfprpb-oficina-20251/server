package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RespostaLoginDTO {
  private String token;
  private long expiresIn;
}
