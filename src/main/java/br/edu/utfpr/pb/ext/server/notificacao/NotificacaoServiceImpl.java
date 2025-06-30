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

  /**
   * Marca uma notificação específica como lida pelo usuário proprietário.
   *
   * <p>Este metodo verifica se o usuário tem permissão para marcar a notificação como lida (deve
   * ser o proprietário) e então atualiza o status da notificação.
   *
   * @param notificacaoId identificador único da notificação a ser marcada como lida
   * @param usuario usuário que está tentando marcar a notificação como lida
   * @throws jakarta.persistence.EntityNotFoundException se a notificação não for encontrada
   * @throws org.springframework.web.server.ResponseStatusException com status 422 se o usuário não
   *     for o proprietário
   */
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

  /**
   * Busca todas as notificações do usuário com paginação e as converte para DTO.
   *
   * <p>Retorna uma página contendo todas as notificações do usuário especificado, ordenadas por
   * data de criação em ordem decrescente (mais recentes primeiro). Os resultados são
   * automaticamente convertidos para DTOs usando ModelMapper.
   *
   * @param usuario usuário proprietário das notificações a serem buscadas
   * @param pageable configuração de paginação (página, tamanho, ordenação)
   * @return página contendo DTOs das notificações do usuário
   * @see br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO
   */
  @Override
  @Transactional(readOnly = true)
  public Page<NotificacaoDTO> buscarNotificacoesDoUsuario(Usuario usuario, Pageable pageable) {
    Page<Notificacao> notificacoes =
        notificacaoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario, pageable);
    return notificacoes.map(element -> modelMapper.map(element, NotificacaoDTO.class));
  }

  /**
   * Busca apenas as notificações não lidas do usuário com paginação.
   *
   * <p>Filtra e retorna uma página contendo somente as notificações não lidas do usuário
   * especificado, ordenadas por data de criação em ordem decrescente. Os resultados são convertidos
   * para DTOs.
   *
   * @param usuario usuário proprietário das notificações não lidas
   * @param pageable configuração de paginação (página, tamanho, ordenação)
   * @return página contendo DTOs das notificações não lidas do usuário
   */
  @Override
  public Page<NotificacaoDTO> buscarNotificacoesNaoLidas(Usuario usuario, Pageable pageable) {
    Page<Notificacao> notificacoes =
        notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataCriacaoDesc(usuario, pageable);
    return notificacoes.map(element -> modelMapper.map(element, NotificacaoDTO.class));
  }

  /**
   * Conta o número total de notificações não lidas de um usuário.
   *
   * <p>Executa uma consulta otimizada para contar apenas as notificações que pertencem ao usuário e
   * ainda não foram marcadas como lidas.
   *
   * @param usuario usuário para o qual será feita a contagem
   * @return número total de notificações não lidas do usuário
   */
  @Override
  public long contarNotificacoesNaoLidas(Usuario usuario) {
    return notificacaoRepository.countByUsuarioAndLidaFalse(usuario);
  }

  /**
   * Marca todas as notificações não lidas do usuário como lidas em uma única operação.
   *
   * <p>Executa uma atualização em lote para marcar todas as notificações não lidas do usuário como
   * lidas, otimizando o desempenho ao evitar múltiplas consultas individuais.
   *
   * @param usuario usuário cujas notificações serão marcadas como lidas
   * @see br.edu.utfpr.pb.ext.server.notificacao.NotificacaoRepository#marcarTodasComoLidas(Usuario)
   */
  @Override
  public void marcarTodasComoLidas(Usuario usuario) {
    notificacaoRepository.marcarTodasComoLidas(usuario);
  }
}
