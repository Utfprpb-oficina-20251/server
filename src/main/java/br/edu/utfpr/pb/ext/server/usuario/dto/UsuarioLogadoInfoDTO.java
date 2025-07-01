package br.edu.utfpr.pb.ext.server.usuario.dto;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioLogadoInfoDTO {
  private Long id;

  @NotBlank(message = "Nome é obrigatório") private String nome;

  private String cpf;
  private String siape;
  private String registroAcademico;

  @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") private String email;

  private String telefone;
  private Long departamentoId;
  private Curso curso;
  private String enderecoCompleto;
}
