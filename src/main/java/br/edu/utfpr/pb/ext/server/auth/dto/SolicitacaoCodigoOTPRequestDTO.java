package br.edu.utfpr.pb.ext.server.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolicitacaoCodigoOTPRequestDTO {
  @Email @NotEmpty private String email;
}
