
package br.edu.utfpr.pb.ext.server.usuario.dto;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(
  name = "UsuarioServidorResponseDTO",
  description = "Dados de um servidor retornados após consulta",
  title = "Resposta de Servidor",
  example = "{\"id\":1,\"nomeCompleto\":\"João da Silva\",\"emailInstitucional\":\"joao@utfpr.edu.br\",\"telefone\":\"41999999999\",\"enderecoCompleto\":\"Rua Exemplo, 123\",\"departamento\":\"DACOM\"}"
)
public class UsuarioServidorResponseDTO {

  @Schema(
    description = "Identificador único do servidor",
    example = "1",
    required = true
  )
  private Long id;

  @Schema(
    description = "Nome completo do servidor",
    example = "João da Silva",
    required = true
  )
  private String nomeCompleto;

  @Schema(
    description = "E-mail institucional do servidor",
    example = "joao@utfpr.edu.br",
    required = true,
    format = "email"
  )
  private String emailInstitucional;

  @Schema(
    description = "Número de telefone do servidor",
    example = "41999999999",
    nullable = true,
    minLength = 11,
    maxLength = 11,
    pattern = "^\\d{11}$"
  )
  private String telefone;

  @Schema(
    description = "Endereço completo do servidor",
    example = "Rua Exemplo, 123, Bairro Centro, Pato Branco/PR",
    nullable = true,
    minLength = 3,
    maxLength = 100
  )
  private String enderecoCompleto;

  @Schema(
    description = "Departamento ao qual o servidor está vinculado",
    example = "DACOM",
    required = true
  )
  private String departamento;
}
