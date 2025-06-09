package br.edu.utfpr.pb.ext.server.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioProjetoDTO {
  private Long id;

  @NotNull private String nome;

  @NotNull @Email(regexp = "^[a-zA-Z0-9._%+-]+@(utfpr\\.edu\\.br)$", message = "{br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO.email}")
  private String email;
}
