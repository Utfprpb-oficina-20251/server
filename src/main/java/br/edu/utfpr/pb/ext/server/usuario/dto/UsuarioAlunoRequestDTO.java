package br.edu.utfpr.pb.ext.server.usuario.dto;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueCpf;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueRa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class UsuarioAlunoRequestDTO {

  private Long id;

  @NotNull private String nome;

  @NotNull @CPF @UniqueCpf private String cpf;

  @NotNull @UniqueRa private String registroAcademico;

  @NotNull @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@(alunos\\.utfpr\\.edu\\.br)$",
      message =
          "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.emailInstitucional}")
  private String email;

  private Curso curso;
}
