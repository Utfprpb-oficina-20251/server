
package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(
  name = "UsuarioCadastradoDTO",
  description = "Dados retornados após o cadastro bem-sucedido de um usuário",
  title = "Usuário Cadastrado",
  example = "{\"id\":1,\"email\":\"usuario@exemplo.com\"}"
)
@Data
public class UsuarioCadastradoDTO {

  @Schema(
    description = "Identificador único do usuário cadastrado",
    example = "1",
    required = true
  )
  private Long id;

  @Schema(
    description = "E-mail do usuário cadastrado",
    example = "usuario@exemplo.com",
    format = "email",
    required = true
  )
  private String email;
}
