package br.edu.utfpr.pb.ext.server.notificacao.dto;

import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacaoDTO {
  private Long id;
  private String titulo;
  private String descricao;
  private TipoNotificacao tipoNotificacao;
  private TipoReferencia tipoReferencia;
  private Long referenciaId;
  private LocalDateTime dataCriacao;
  private boolean lida;
}
