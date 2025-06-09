package br.edu.utfpr.pb.ext.server.usuario.dto;

import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueCpf;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueSiape;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

@Data
public class UsuarioServidorRequestDTO {

  private Long id;

  @NotNull private String nome;

  @NotNull @CPF @UniqueCpf private String cpf;

  @NotNull @UniqueSiape
  @Size(
      min = 7,
      max = 7,
      message = "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.siape}")
  private String siape;

  @NotNull @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$",
      message =
          "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.email}")
  private String email;

  @Size(min = 11, max = 11) private String telefone;

  @Size(min = 3, max = 100) private String enderecoCompleto;

  @NotNull private Departamentos departamento;
}
