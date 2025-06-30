package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificacaoService extends ICrudService<Notificacao, Long> {
  void marcarComoLida(Long notificacaoId, Usuario usuario);

  Page<NotificacaoDTO> buscarNotificacoesDoUsuario(Usuario usuario, Pageable pageable);

  Page<NotificacaoDTO> buscarNotificacoesNaoLidas(Usuario usuario, Pageable pageable);

  long contarNotificacoesNaoLidas(Usuario usuario);

  void marcarTodasComoLidas(Usuario usuario);
}
