package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificacaoService extends ICrudService<Notificacao, Long> {
  void marcarComoLida(Long notificacaoId, Usuario usuario);

  Page<NotificacaoDTO> buscarNotificacoesDoUsuario(Usuario usuario, Pageable pageable);

  Page<NotificacaoDTO> buscarNotificacoesNaoLidas(Usuario usuario, Pageable pageable);

  long contarNotificacoesNaoLidas(Usuario usuario);

  void marcarTodasComoLidas(Usuario usuario);

  void criarNotificacaoParaMultiplosUsuarios(
      List<Usuario> destinatarios,
      String titulo,
      String descricao,
      TipoNotificacao tipo,
      TipoReferencia tipoReferencia,
      Long referenciaId);
}
