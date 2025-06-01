package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolicitacaoCodigoDTO {
  private String mensagem;
}
