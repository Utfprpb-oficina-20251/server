
package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(
  name = "LoginUsuarioDTO",
  description = "Dados para autenticação do usuário no sistema",
  title = "Login de Usuário",
  example = "{\"email\":\"usuario@exemplo.com\",\"senha\":\"senha_segura\"}"
)
public class LoginUsuarioDTO {

  @Schema(
    description = "E-mail do usuário para autenticação",
    example = "usuario@exemplo.com",
    format = "email",
    required = true
  )
  private @NotBlank @Email String email;

  @Schema(
    description = "Senha do usuário",
    example = "senha_segura",
    required = true,
    format = "password",
    minLength = 6
  )
  private @NotBlank String senha;
}
