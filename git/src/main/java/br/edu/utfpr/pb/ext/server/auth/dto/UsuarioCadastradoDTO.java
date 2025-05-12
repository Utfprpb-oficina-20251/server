package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.Data;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@Schema(
  name = "UsuarioCadastradoDTO",
  description = "Dados retornados após o cadastro bem-sucedido de um usuário",
  title = "Usuário Cadastrado",
  example = "{\"nome\":\"João da Silva\",\"email\":\"usuario@exemplo.com\"}"
)
public class UsuarioCadastradoDTO {

    @Schema(
      description = "Nome completo do usuário cadastrado",
      example = "João da Silva",
      required = true,
      minLength = 3,
      maxLength = 100
    )
    private String nome;

    @Schema(
      description = "E-mail do usuário cadastrado",
      example = "usuario@exemplo.com",
      format = "email",
      required = true
    )
    private String email;

}