package br.edu.utfpr.pb.ext.server.event;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.notificacao.NotificacaoService;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener responsável por criar notificações baseadas em eventos do sistema.
 *
 * <p>Este componente escuta eventos de entidades (criação, atualização) e gera notificações
 * apropriadas para os usuários relevantes, seguindo o mesmo padrão do sistema de emails.
 *
 * <p>As notificações são processadas de forma assíncrona e transacional para garantir performance e
 * consistência dos dados.
 *
 * <p>Os eventos são processados após o commit da transação principal para garantir que as
 * notificações só sejam criadas quando as operações principais foram bem-sucedidas.
 *
 * @author Sistema de Extensão UTFPR-PB
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoEventListener {

  private final NotificacaoService notificacaoService;

  /**
   * Processa eventos relacionados a projetos e cria notificações apropriadas.
   *
   * <p>Para projetos, as notificações são enviadas apenas para:
   *
   * <ul>
   *   <li>Membros da equipe executora
   * </ul>
   *
   * <p>Usa {@link TransactionalEventListener} para garantir que as notificações só sejam criadas
   * após o commit da transação principal.
   *
   * @param event evento de projeto contendo a entidade e o tipo de operação
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProjetoEvent(ProjetoEvent event) {
    try {
      var projeto = event.getEntity();

      if (projeto == null) {
        log.warn("Evento de projeto recebido com entidade nula");
        return;
      }

      Set<Usuario> destinatarios = coletarDestinatariosProjeto(projeto);

      if (destinatarios.isEmpty()) {
        log.debug(
            "Nenhum membro da equipe para notificar no projeto: {} (ID: {})",
            projeto.getTitulo(),
            projeto.getId());
        return;
      }

      var configuracaoNotificacao = criarConfiguracaoNotificacaoProjeto(event, projeto);

      criarNotificacoes(
          new ArrayList<>(destinatarios),
          configuracaoNotificacao,
          TipoReferencia.PROJETO,
          projeto.getId(),
          "projeto",
          projeto.getTitulo());

    } catch (Exception e) {
      log.error("Erro ao processar evento de projeto: {}", e.getMessage(), e);
    }
  }

  /**
   * Processa eventos relacionados a sugestões de projeto.
   *
   * <p>Para sugestões, as notificações são enviadas para:
   *
   * <ul>
   *   <li>Aluno que criou a sugestão
   *   <li>Professor associado à sugestão
   * </ul>
   *
   * @param event evento de sugestão contendo a entidade e o tipo de operação
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleSugestaoEvent(SugestaoEvent event) {
    try {
      var sugestao = event.getEntity();

      if (sugestao == null) {
        log.warn("Evento de sugestão recebido com entidade nula");
        return;
      }

      Set<Usuario> destinatarios = coletarDestinatariosSugestao(sugestao);

      if (destinatarios.isEmpty()) {
        log.debug(
            "Nenhum destinatário encontrado para notificar na sugestão: {} (ID: {})",
            sugestao.getTitulo(),
            sugestao.getId());
        return;
      }

      var configuracaoNotificacao = criarConfiguracaoNotificacaoSugestao(event, sugestao);

      criarNotificacoes(
          new ArrayList<>(destinatarios),
          configuracaoNotificacao,
          TipoReferencia.SUGESTAO_PROJETO,
          sugestao.getId(),
          "sugestão",
          sugestao.getTitulo());

    } catch (Exception e) {
      log.error("Erro ao processar evento de sugestão: {}", e.getMessage(), e);
    }
  }

  /**
   * Processa eventos relacionados a candidaturas.
   *
   * <p>Para candidaturas, as notificações são enviadas para:
   *
   * <ul>
   *   <li>Aluno que fez a candidatura
   *   <li>Responsável do projeto
   * </ul>
   *
   * @param event evento de candidatura contendo a entidade e o tipo de operação
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleCandidaturaEvent(CandidaturaEvent event) {
    try {
      var candidatura = event.getEntity();

      if (candidatura == null) {
        log.warn("Evento de candidatura recebido com entidade nula");
        return;
      }

      Set<Usuario> destinatarios = coletarDestinatariosCandidatura(candidatura);

      if (destinatarios.isEmpty()) {
        log.debug(
            "Nenhum destinatário encontrado para notificar na candidatura do projeto: {} (ID: {})",
            candidatura.getProjeto() != null ? candidatura.getProjeto().getTitulo() : "N/A",
            candidatura.getId());
        return;
      }

      var configuracaoNotificacao = criarConfiguracaoNotificacaoCandidatura(event, candidatura);
      String nomeProjeto =
          candidatura.getProjeto() != null ? candidatura.getProjeto().getTitulo() : "Projeto";

      criarNotificacoes(
          new ArrayList<>(destinatarios),
          configuracaoNotificacao,
          TipoReferencia.CANDIDATURA,
          candidatura.getId(),
          "candidatura",
          nomeProjeto);

    } catch (Exception e) {
      log.error("Erro ao processar evento de candidatura: {}", e.getMessage(), e);
    }
  }

  /**
   * Coleta os destinatários de notificações para eventos de projeto.
   *
   * <p>Para projetos, apenas os membros da equipe executora recebem notificações.
   *
   * @param projeto projeto do qual coletar os destinatários
   * @return conjunto de usuários que devem receber notificações
   */
  private Set<Usuario> coletarDestinatariosProjeto(Projeto projeto) {
    Set<Usuario> destinatarios = new HashSet<>();

    if (projeto.getEquipeExecutora() != null && !projeto.getEquipeExecutora().isEmpty()) {
      destinatarios.addAll(projeto.getEquipeExecutora());
    }

    return destinatarios;
  }

  /**
   * Coleta os destinatários de notificações para eventos de sugestão.
   *
   * <p>Para sugestões, tanto o aluno quanto o professor associados recebem notificações.
   *
   * @param sugestao sugestão da qual coletar os destinatários
   * @return conjunto de usuários que devem receber notificações
   */
  private Set<Usuario> coletarDestinatariosSugestao(SugestaoDeProjeto sugestao) {
    Set<Usuario> destinatarios = new HashSet<>();

    if (sugestao.getAluno() != null) {
      destinatarios.add(sugestao.getAluno());
    }

    if (sugestao.getProfessor() != null) {
      destinatarios.add(sugestao.getProfessor());
    }

    return destinatarios;
  }

  /**
   * Coleta os destinatários de notificações para eventos de candidatura.
   *
   * <p>Para candidaturas, o aluno candidato e o responsável pelo projeto recebem notificações.
   *
   * @param candidatura candidatura da qual coletar os destinatários
   * @return conjunto de usuários que devem receber notificações
   */
  private Set<Usuario> coletarDestinatariosCandidatura(Candidatura candidatura) {
    Set<Usuario> destinatarios = new HashSet<>();

    if (candidatura.getAluno() != null) {
      destinatarios.add(candidatura.getAluno());
    }

    if (candidatura.getProjeto() != null && candidatura.getProjeto().getResponsavel() != null) {
      destinatarios.add(candidatura.getProjeto().getResponsavel());
    }

    return destinatarios;
  }

  /**
   * Cria a configuração de notificação apropriada para eventos de projeto.
   *
   * @param event evento de projeto
   * @param projeto projeto relacionado ao evento
   * @return configuração da notificação ou null se o tipo de evento não for suportado
   */
  private ConfiguracaoNotificacao criarConfiguracaoNotificacaoProjeto(
      ProjetoEvent event, Projeto projeto) {
    return switch (event.getEventType()) {
      case CREATED ->
          new ConfiguracaoNotificacao(
              "Novo Projeto Criado",
              String.format(
                  "O projeto '%s' foi criado e você foi incluído como membro.",
                  projeto.getTitulo()),
              TipoNotificacao.SUCESSO);
      case UPDATED ->
          new ConfiguracaoNotificacao(
              "Projeto Atualizado",
              String.format(
                  "O projeto '%s' foi atualizado. Verifique as alterações realizadas.",
                  projeto.getTitulo()),
              TipoNotificacao.INFO);
      default -> {
        log.debug(
            "Tipo de evento não processado para notificações de projeto: {}", event.getEventType());
        yield null;
      }
    };
  }

  /**
   * Cria a configuração de notificação apropriada para eventos de sugestão.
   *
   * @param event evento de sugestão
   * @param sugestao sugestão relacionada ao evento
   * @return configuração da notificação ou null se o tipo de evento não for suportado
   */
  private ConfiguracaoNotificacao criarConfiguracaoNotificacaoSugestao(
      SugestaoEvent event, SugestaoDeProjeto sugestao) {
    return switch (event.getEventType()) {
      case CREATED ->
          new ConfiguracaoNotificacao(
              "Nova Sugestão Registrada",
              String.format(
                  "A sugestão '%s' foi registrada com sucesso no sistema.", sugestao.getTitulo()),
              TipoNotificacao.SUCESSO);
      case UPDATED ->
          new ConfiguracaoNotificacao(
              "Sugestão Atualizada",
              String.format(
                  "A sugestão '%s' foi atualizada. Verifique as alterações realizadas.",
                  sugestao.getTitulo()),
              TipoNotificacao.INFO);
      default -> {
        log.debug(
            "Tipo de evento não processado para notificações de sugestão: {}",
            event.getEventType());
        yield null;
      }
    };
  }

  /**
   * Cria a configuração de notificação apropriada para eventos de candidatura.
   *
   * @param event evento de candidatura
   * @param candidatura candidatura relacionada ao evento
   * @return configuração da notificação ou null se o tipo de evento não for suportado
   */
  private ConfiguracaoNotificacao criarConfiguracaoNotificacaoCandidatura(
      CandidaturaEvent event, Candidatura candidatura) {
    String nomeProjeto =
        candidatura.getProjeto() != null ? candidatura.getProjeto().getTitulo() : "Projeto";
    String nomeAluno = candidatura.getAluno() != null ? candidatura.getAluno().getNome() : "N/A";

    return switch (event.getEventType()) {
      case CREATED ->
          new ConfiguracaoNotificacao(
              "Nova Candidatura Recebida",
              String.format("O aluno %s se candidatou ao projeto '%s'.", nomeAluno, nomeProjeto),
              TipoNotificacao.INFO);
      case UPDATED ->
          new ConfiguracaoNotificacao(
              "Status da Candidatura Atualizado",
              String.format(
                  "O status da candidatura de %s para o projeto '%s' foi atualizado para: %s",
                  nomeAluno, nomeProjeto, candidatura.getStatus()),
              TipoNotificacao.INFO);
      default -> {
        log.debug(
            "Tipo de evento não processado para notificações de candidatura: {}",
            event.getEventType());
        yield null;
      }
    };
  }

  /**
   * Método centralizado para criação de notificações em lote.
   *
   * <p>Este método coordena a criação das notificações através do serviço e registra logs
   * informativos sobre o processo.
   *
   * @param destinatarios lista de usuários que receberão a notificação
   * @param config configuração da notificação (título, descrição, tipo)
   * @param tipoReferencia tipo de referência da entidade
   * @param referenciaId ID da entidade referenciada
   * @param tipoEntidade nome do tipo de entidade para logs
   * @param nomeEntidade nome da entidade para logs
   */
  private void criarNotificacoes(
      List<Usuario> destinatarios,
      ConfiguracaoNotificacao config,
      TipoReferencia tipoReferencia,
      Long referenciaId,
      String tipoEntidade,
      String nomeEntidade) {

    if (config == null) {
      return;
    }

    try {
      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          config.titulo(),
          config.descricao(),
          config.tipo(),
          tipoReferencia,
          referenciaId);

      log.info(
          "Notificações criadas para {} '{}' (ID: {}) - {} destinatários: [{}]",
          tipoEntidade,
          nomeEntidade,
          referenciaId,
          destinatarios.size(),
          destinatarios.stream().map(Usuario::getEmail).collect(Collectors.joining(", ")));

    } catch (Exception e) {
      log.error(
          "Erro ao criar notificação para {} '{}' (ID: {}): {}",
          tipoEntidade,
          nomeEntidade,
          referenciaId,
          e.getMessage(),
          e);
    }
  }

  /**
   * Record que encapsula a configuração de uma notificação.
   *
   * <p>Contém as informações básicas necessárias para criar uma notificação: título, descrição e
   * tipo.
   *
   * @param titulo título da notificação
   * @param descricao descrição detalhada da notificação
   * @param tipo tipo da notificação (INFO, SUCESSO, etc.)
   */
  private record ConfiguracaoNotificacao(String titulo, String descricao, TipoNotificacao tipo) {}
}
