package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginUsuarioDTO {
  private @NotBlank String email;
  private @NotBlank String senha;
}
