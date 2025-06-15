package br.edu.utfpr.pb.ext.server.usuario.dto;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
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
  private String nome;
  private String cpf;
  private String siape;
  private String registroAcademico;
  private String email;
  private String telefone;
  private Departamentos departamento;
  private Curso curso;
  private String enderecoCompleto;
}
