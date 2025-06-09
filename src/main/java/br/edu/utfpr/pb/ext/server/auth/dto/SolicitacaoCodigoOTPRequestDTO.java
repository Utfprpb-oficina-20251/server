package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolicitacaoCodigoOTPRequestDTO {
  @Email(
      regexp = "^[a-zA-Z0-9._%+-]+@(alunos\\.utfpr\\.edu\\.br|utfpr\\.edu\\.br)$",
      message = "E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br")
  @NotEmpty private String email;
}
