package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificacaoServiceImpl extends CrudServiceImpl<Notificacao, Long>
    implements NotificacaoService {
  private final NotificacaoRepository notificacaoRepository;
  private final ModelMapper modelMapper;

  @Override
  protected JpaRepository<Notificacao, Long> getRepository() {
    return this.notificacaoRepository;
  }

  @Override
  public void marcarComoLida(Long notificacaoId, Usuario usuario) {
    Notificacao notificacao = findOne(notificacaoId);
    if (!notificacao.pertenceAoUsuario(usuario)) {
      throw new ResponseStatusException(
          HttpStatus.UNPROCESSABLE_ENTITY, "Usuário não tem permissão para marcar como lida");
    }
    notificacao.marcarComoLida();
    super.save(notificacao);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<NotificacaoDTO> buscarNotificacoesDoUsuario(Usuario usuario, Pageable pageable) {
    Page<Notificacao> notificacoes =
        notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuario, pageable);
    return notificacoes.map(element -> modelMapper.map(element, NotificacaoDTO.class));
  }

  @Override
  public Page<NotificacaoDTO> buscarNotificacoesNaoLidas(Usuario usuario, Pageable pageable) {
    Page<Notificacao> notificacoes =
        notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByDataCriacaoDesc(usuario, pageable);
    return notificacoes.map(element -> modelMapper.map(element, NotificacaoDTO.class));
  }

  @Override
  public long contarNotificacoesNaoLidas(Usuario usuario) {
    return notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
  }

  @Override
  public void marcarTodasComoLidas(Usuario usuario) {
    notificacaoRepository.marcarTodasComoLidas(usuario);
  }
}
