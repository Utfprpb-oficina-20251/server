
package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(
  name = "CadastroUsuarioDTO",
  description = "Dados necessários para o cadastro de um novo usuário no sistema",
  title = "Cadastro de Usuário",
  example = "{\"email\":\"usuario@exemplo.com\",\"nome\":\"João da Silva\",\"registro\":\"123456789\"}"
)
public class CadastroUsuarioDTO {

  @Schema(
    description = "E-mail do usuário para acesso ao sistema",
    example = "usuario@exemplo.com",
    format = "email",
    required = true,
    maxLength = 255
  )
  private @Email String email;

  @Schema(
    description = "Nome completo do usuário",
    example = "João da Silva",
    required = true,
    minLength = 3,
    maxLength = 100
  )
  private @NotBlank String nome;

  @Schema(
    description = "Registro acadêmico ou funcional do usuário",
    example = "123456789",
    required = true,
    pattern = "^[A-Za-z0-9]{3,20}$",
    minLength = 3,
    maxLength = 20
  )
  private @NotBlank String registro;
}
