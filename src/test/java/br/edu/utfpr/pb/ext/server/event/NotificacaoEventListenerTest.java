package br.edu.utfpr.pb.ext.server.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.candidatura.Candidatura;
import br.edu.utfpr.pb.ext.server.candidatura.StatusCandidatura;
import br.edu.utfpr.pb.ext.server.notificacao.NotificacaoService;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.SugestaoDeProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoEventListener")
class NotificacaoEventListenerTest {

  @Mock private NotificacaoService notificacaoService;

  @InjectMocks private NotificacaoEventListener notificacaoEventListener;

  private Usuario usuario1;
  private Usuario usuario2;
  private Usuario professor;
  private Usuario aluno;
  private Usuario responsavel;

  @BeforeEach
  void setUp() {
    usuario1 = new Usuario();
    usuario1.setId(1L);
    usuario1.setEmail("usuario1@test.com");
    usuario1.setNome("Usuario 1");

    usuario2 = new Usuario();
    usuario2.setId(2L);
    usuario2.setEmail("usuario2@test.com");
    usuario2.setNome("Usuario 2");

    professor = new Usuario();
    professor.setId(3L);
    professor.setEmail("professor@test.com");
    professor.setNome("Professor");

    aluno = new Usuario();
    aluno.setId(4L);
    aluno.setEmail("aluno@test.com");
    aluno.setNome("Aluno");

    responsavel = new Usuario();
    responsavel.setId(5L);
    responsavel.setEmail("responsavel@test.com");
    responsavel.setNome("Responsável");
  }

  @Test
  @DisplayName("handleProjetoEvent - deve criar notificações para evento CREATED")
  void handleProjetoEvent_deveCriarNotificacoesParaEventoCreated() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");
    projeto.setEquipeExecutora(Arrays.asList(usuario1, usuario2));

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);
    ArgumentCaptor<TipoReferencia> tipoReferenciaCaptor =
        ArgumentCaptor.forClass(TipoReferencia.class);
    ArgumentCaptor<Long> referenciaIdCaptor = ArgumentCaptor.forClass(Long.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            tipoReferenciaCaptor.capture(),
            referenciaIdCaptor.capture());

    assertEquals(2, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(usuario1));
    assertTrue(destinatariosCaptor.getValue().contains(usuario2));
    assertEquals("Novo Projeto Criado", tituloCaptor.getValue());
    assertEquals(
        "O projeto 'Projeto Teste' foi criado e você foi incluído como membro.",
        descricaoCaptor.getValue());
    assertEquals(TipoNotificacao.SUCESSO, tipoCaptor.getValue());
    assertEquals(TipoReferencia.PROJETO, tipoReferenciaCaptor.getValue());
    assertEquals(1L, referenciaIdCaptor.getValue());
  }

  @Test
  @DisplayName("handleProjetoEvent - deve criar notificações para evento UPDATED")
  void handleProjetoEvent_deveCriarNotificacoesParaEventoUpdated() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Atualizado");
    projeto.setEquipeExecutora(Collections.singletonList(usuario1));

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.UPDATED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            any(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            any(),
            any());

    assertEquals("Projeto Atualizado", tituloCaptor.getValue());
    assertEquals(
        "O projeto 'Projeto Atualizado' foi atualizado. Verifique as alterações realizadas.",
        descricaoCaptor.getValue());
    assertEquals(TipoNotificacao.INFO, tipoCaptor.getValue());
  }

  @Test
  @DisplayName("handleProjetoEvent - não deve criar notificações quando equipe executora é vazia")
  void handleProjetoEvent_naoDeveCriarNotificacoesQuandoEquipeExecutoraEhVazia() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Sem Equipe");
    projeto.setEquipeExecutora(new ArrayList<>());

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("handleProjetoEvent - não deve criar notificações quando equipe executora é nula")
  void handleProjetoEvent_naoDeveCriarNotificacoesQuandoEquipeExecutoraEhNula() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Sem Equipe");
    projeto.setEquipeExecutora(null);

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("handleProjetoEvent - não deve criar notificações para evento DELETED")
  void handleProjetoEvent_naoDeveCriarNotificacoesParaEventoDeleted() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Deletado");
    projeto.setEquipeExecutora(Collections.singletonList(usuario1));

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.DELETED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("handleSugestaoEvent - deve criar notificações para evento CREATED")
  void handleSugestaoEvent_deveCriarNotificacoesParaEventoCreated() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Teste");
    sugestao.setAluno(aluno);
    sugestao.setProfessor(professor);

    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleSugestaoEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);
    ArgumentCaptor<TipoReferencia> tipoReferenciaCaptor =
        ArgumentCaptor.forClass(TipoReferencia.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            tipoReferenciaCaptor.capture(),
            any());

    assertEquals(2, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(aluno));
    assertTrue(destinatariosCaptor.getValue().contains(professor));
    assertEquals("Nova Sugestão Registrada", tituloCaptor.getValue());
    assertEquals(
        "A sugestão 'Sugestão Teste' foi registrada com sucesso no sistema.",
        descricaoCaptor.getValue());
    assertEquals(TipoNotificacao.SUCESSO, tipoCaptor.getValue());
    assertEquals(TipoReferencia.SUGESTAO_PROJETO, tipoReferenciaCaptor.getValue());
  }

  @Test
  @DisplayName(
      "handleSugestaoEvent - deve criar notificações apenas para o aluno quando professor é nulo")
  void handleSugestaoEvent_deveCriarNotificacoesApenasParaAlunoQuandoProfessorEhNulo() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Apenas Aluno");
    sugestao.setAluno(aluno);
    sugestao.setProfessor(null);

    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleSugestaoEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(), any(), any(), any(), any(), any());

    assertEquals(1, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(aluno));
    assertFalse(destinatariosCaptor.getValue().contains(professor));
  }

  @Test
  @DisplayName("handleSugestaoEvent - deve criar notificações para evento UPDATED")
  void handleSugestaoEvent_deveCriarNotificacoesParaEventoUpdated() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Atualizada");
    sugestao.setAluno(aluno);

    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.UPDATED);

    // Act
    notificacaoEventListener.handleSugestaoEvent(event);

    // Assert
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            any(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            any(),
            any());

    assertEquals("Sugestão Atualizada", tituloCaptor.getValue());
    assertEquals(
        "A sugestão 'Sugestão Atualizada' foi atualizada. Verifique as alterações realizadas.",
        descricaoCaptor.getValue());
    assertEquals(TipoNotificacao.INFO, tipoCaptor.getValue());
  }

  @Test
  @DisplayName("handleSugestaoEvent - não deve criar notificações quando não há destinatários")
  void handleSugestaoEvent_naoDeveCriarNotificacoesQuandoNaoHaDestinatarios() {
    // Arrange
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(1L);
    sugestao.setTitulo("Sugestão Sem Destinatários");
    sugestao.setAluno(null);
    sugestao.setProfessor(null);

    SugestaoEvent event = new SugestaoEvent(sugestao, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleSugestaoEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("handleCandidaturaEvent - deve criar notificações para evento CREATED")
  void handleCandidaturaEvent_deveCriarNotificacoesParaEventoCreated() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Candidatura");
    projeto.setResponsavel(responsavel);

    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno);
    candidatura.setProjeto(projeto);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);
    ArgumentCaptor<TipoReferencia> tipoReferenciaCaptor =
        ArgumentCaptor.forClass(TipoReferencia.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            tipoReferenciaCaptor.capture(),
            any());

    assertEquals(2, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(aluno));
    assertTrue(destinatariosCaptor.getValue().contains(responsavel));
    assertEquals("Nova Candidatura Recebida", tituloCaptor.getValue());
    assertEquals(
        "O aluno Aluno se candidatou ao projeto 'Projeto Candidatura'.",
        descricaoCaptor.getValue());
    assertEquals(TipoNotificacao.INFO, tipoCaptor.getValue());
    assertEquals(TipoReferencia.CANDIDATURA, tipoReferenciaCaptor.getValue());
  }

  @Test
  @DisplayName("handleCandidaturaEvent - deve criar notificações para evento UPDATED")
  void handleCandidaturaEvent_deveCriarNotificacoesParaEventoUpdated() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Candidatura");
    projeto.setResponsavel(responsavel);

    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno);
    candidatura.setProjeto(projeto);
    candidatura.setStatus(StatusCandidatura.APROVADA);

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.UPDATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    ArgumentCaptor<String> tituloCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> descricaoCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<TipoNotificacao> tipoCaptor = ArgumentCaptor.forClass(TipoNotificacao.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            any(),
            tituloCaptor.capture(),
            descricaoCaptor.capture(),
            tipoCaptor.capture(),
            any(),
            any());

    assertEquals("Status da Candidatura Atualizado", tituloCaptor.getValue());
    assertTrue(descricaoCaptor.getValue().contains(StatusCandidatura.APROVADA.toString()));
    assertEquals(TipoNotificacao.INFO, tipoCaptor.getValue());
  }

  @Test
  @DisplayName(
      "handleCandidaturaEvent - deve criar notificações apenas para aluno quando projeto é nulo")
  void handleCandidaturaEvent_deveCriarNotificacoesApenasParaAlunoQuandoProjetoEhNulo() {
    // Arrange
    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno);
    candidatura.setProjeto(null);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(), any(), any(), any(), any(), any());

    assertEquals(1, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(aluno));
  }

  @Test
  @DisplayName(
      "handleCandidaturaEvent - deve usar valores padrão quando projeto ou aluno são nulos")
  void handleCandidaturaEvent_deveUsarValoresPadraoQuandoProjetoOuAlunoSaoNulos() {
    // Arrange
    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(null);
    candidatura.setProjeto(null);
    candidatura.setStatus(StatusCandidatura.PENDENTE);

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("handleCandidaturaEvent - deve lidar com valores nulos graciosamente")
  void handleCandidaturaEvent_deveLidarComValoresNulosGraciosamente() {
    // Arrange
    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    // Não define aluno, projeto ou responsável

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    verify(notificacaoService, never())
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("deve evitar destinatários duplicados")
  void deveEvitarDestinatariosDuplicados() {
    // Arrange - Criando cenário onde mesmo usuário está como aluno e responsável
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Candidatura");
    projeto.setResponsavel(aluno); // Mesmo usuário como responsável

    Candidatura candidatura = new Candidatura();
    candidatura.setId(1L);
    candidatura.setAluno(aluno); // Mesmo usuário como aluno
    candidatura.setProjeto(projeto);

    CandidaturaEvent event = new CandidaturaEvent(candidatura, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleCandidaturaEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(), any(), any(), any(), any(), any());

    // Deve ter apenas 1 destinatário (sem duplicação)
    assertEquals(1, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(aluno));
  }

  @Test
  @DisplayName("deve lidar com exceções do NotificacaoService graciosamente")
  void deveLidarComExcecoesDoNotificacaoServiceGraciosamente() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");
    projeto.setEquipeExecutora(Collections.singletonList(usuario1));

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    doThrow(new RuntimeException("Erro no serviço"))
        .when(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());

    // Act & Assert
    assertDoesNotThrow(() -> notificacaoEventListener.handleProjetoEvent(event));
    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("deve processar projeto com equipe executora contendo usuários duplicados")
  void deveProcessarProjetoComEquipeExecutoraContendoUsuariosDuplicados() {
    // Arrange
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto com Duplicados");
    // Lista com usuários duplicados
    projeto.setEquipeExecutora(Arrays.asList(usuario1, usuario2, usuario1));

    ProjetoEvent event = new ProjetoEvent(projeto, EntityEvent.EventType.CREATED);

    // Act
    notificacaoEventListener.handleProjetoEvent(event);

    // Assert
    ArgumentCaptor<List<Usuario>> destinatariosCaptor = ArgumentCaptor.forClass(List.class);

    verify(notificacaoService)
        .criarNotificacaoParaMultiplosUsuarios(
            destinatariosCaptor.capture(), any(), any(), any(), any(), any());

    // Deve remover duplicados (Set behavior)
    assertEquals(2, destinatariosCaptor.getValue().size());
    assertTrue(destinatariosCaptor.getValue().contains(usuario1));
    assertTrue(destinatariosCaptor.getValue().contains(usuario2));
  }
}
