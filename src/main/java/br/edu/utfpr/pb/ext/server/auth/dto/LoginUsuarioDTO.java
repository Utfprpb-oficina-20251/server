package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginUsuarioDTO {
  private @NotBlank @Email String email;
  private @NotBlank String senha;
}
