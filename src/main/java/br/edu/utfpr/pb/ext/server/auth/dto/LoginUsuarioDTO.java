package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUsuarioDTO {
  private @NotBlank String email;
  private @NotBlank String senha;
}
