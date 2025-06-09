package br.edu.utfpr.pb.ext.server.projeto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelamentoProjetoDTO {
  @NotBlank private String justificativa;
}
