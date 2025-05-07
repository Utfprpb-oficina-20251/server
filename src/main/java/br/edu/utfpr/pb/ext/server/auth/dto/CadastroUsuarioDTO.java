package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CadastroUsuarioDTO {
  private @Email String email;
  private @NotBlank String nome;
  private @NotBlank String registro;
}
