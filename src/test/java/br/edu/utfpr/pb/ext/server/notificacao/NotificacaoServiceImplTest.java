package br.edu.utfpr.pb.ext.server.notificacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoReferencia;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceImplTest {

  private static final String TITULO_TESTE = "Título Teste";
  private static final String DESCRICAO_TESTE = "Descrição Teste";
  private static final Long REFERENCIA_ID_TESTE = 1L;
  private static final int TOTAL_USUARIOS_TESTE = 3;

  @Mock private NotificacaoRepository notificacaoRepository;
  @Mock private ModelMapper modelMapper;
  @InjectMocks private NotificacaoServiceImpl notificacaoService;

  private Usuario usuario1;
  private Usuario usuario2;
  private Usuario usuario3;
  private Notificacao notificacao1;
  private Notificacao notificacao2;
  private NotificacaoDTO notificacaoDTO1;
  private NotificacaoDTO notificacaoDTO2;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    usuario1 = createUsuario(1L, "Usuario 1", "usuario1@test.com");
    usuario2 = createUsuario(2L, "Usuario 2", "usuario2@test.com");
    usuario3 = createUsuario(3L, "Usuario 3", "usuario3@test.com");

    notificacao1 = createSimpleNotificacao(1L, usuario1, false);
    notificacao2 = createSimpleNotificacao(2L, usuario1, true);

    notificacaoDTO1 = new NotificacaoDTO();
    notificacaoDTO1.setId(1L);
    notificacaoDTO1.setTitulo(TITULO_TESTE);

    notificacaoDTO2 = new NotificacaoDTO();
    notificacaoDTO2.setId(2L);
    notificacaoDTO2.setTitulo(TITULO_TESTE);

    pageable = PageRequest.of(0, 10);
  }

  private Usuario createUsuario(Long id, String nome, String email) {
    return Usuario.builder().id(id).nome(nome).email(email).build();
  }

  private Notificacao createTestNotificacao(
      Long id,
      String titulo,
      String descricao,
      TipoNotificacao tipo,
      TipoReferencia tipoReferencia,
      Long referenciaId,
      Usuario usuario,
      boolean lida) {
    return Notificacao.builder()
        .id(id)
        .titulo(titulo)
        .descricao(descricao)
        .tipoNotificacao(tipo)
        .tipoReferencia(tipoReferencia)
        .referenciaId(referenciaId)
        .dataCriacao(LocalDateTime.now())
        .lida(lida)
        .usuario(usuario)
        .build();
  }

  private Notificacao createSimpleNotificacao(Long id, Usuario usuario, boolean lida) {
    return createTestNotificacao(
        id,
        TITULO_TESTE,
        DESCRICAO_TESTE,
        TipoNotificacao.INFO,
        TipoReferencia.PROJETO,
        REFERENCIA_ID_TESTE,
        usuario,
        lida);
  }

  private List<Notificacao> capturarNotificacoesSalvas() {
    ArgumentCaptor<List<Notificacao>> captor = ArgumentCaptor.forClass(List.class);
    verify(notificacaoRepository).saveAll(captor.capture());
    return captor.getValue();
  }

  private void assertNotificacaoBasica(
      Notificacao notificacao,
      String titulo,
      String descricao,
      TipoNotificacao tipo,
      Usuario usuario) {
    assertEquals(titulo, notificacao.getTitulo());
    assertEquals(descricao, notificacao.getDescricao());
    assertEquals(tipo, notificacao.getTipoNotificacao());
    assertEquals(usuario, notificacao.getUsuario());
    assertFalse(notificacao.isLida());
    assertNotNull(notificacao.getDataCriacao());
  }

  @Test
  @DisplayName("getRepository deve retornar NotificacaoRepository")
  void getRepository_deveRetornarNotificacaoRepository() {
    assertEquals(notificacaoRepository, notificacaoService.getRepository());
  }

  @Nested
  @DisplayName("Testes para marcarComoLida")
  class MarcarComoLidaTests {

    @Test
    @DisplayName("deve marcar notificacao como lida quando usuario eh proprietario")
    void deveMarcarNotificacaoComoLidaQuandoUsuarioEhProprietario() {
      when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao1));

      notificacaoService.marcarComoLida(1L, usuario1);

      assertTrue(notificacao1.isLida());
      verify(notificacaoRepository).save(notificacao1);
    }

    @Test
    @DisplayName("deve lancar exception quando usuario nao eh proprietario")
    void deveLancarExceptionQuandoUsuarioNaoEhProprietario() {
      when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao1));

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class, () -> notificacaoService.marcarComoLida(1L, usuario2));

      assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
      assertFalse(notificacao1.isLida());
      verify(notificacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lancar EntityNotFoundException quando notificacao nao existe")
    void deveLancarEntityNotFoundExceptionQuandoNotificacaoNaoExiste() {
      when(notificacaoRepository.findById(99L)).thenReturn(Optional.empty());

      assertThrows(
          EntityNotFoundException.class, () -> notificacaoService.marcarComoLida(99L, usuario1));

      verify(notificacaoRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("Testes para busca de notificacoes")
  class BuscarNotificacoesTests {

    @Test
    @DisplayName("buscarNotificacoesDoUsuario deve retornar page de NotificacaoDTO")
    void buscarNotificacoesDoUsuario_deveRetornarPageDeNotificacaoDTO() {
      List<Notificacao> notificacoes = Arrays.asList(notificacao1, notificacao2);
      Page<Notificacao> pageNotificacoes =
          new PageImpl<>(notificacoes, pageable, notificacoes.size());

      when(notificacaoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario1, pageable))
          .thenReturn(pageNotificacoes);
      when(modelMapper.map(notificacao1, NotificacaoDTO.class)).thenReturn(notificacaoDTO1);
      when(modelMapper.map(notificacao2, NotificacaoDTO.class)).thenReturn(notificacaoDTO2);

      Page<NotificacaoDTO> result =
          notificacaoService.buscarNotificacoesDoUsuario(usuario1, pageable);

      assertEquals(2, result.getContent().size());
      assertEquals(notificacaoDTO1, result.getContent().get(0));
      assertEquals(notificacaoDTO2, result.getContent().get(1));
      verify(notificacaoRepository).findByUsuarioOrderByDataCriacaoDesc(usuario1, pageable);
    }

    @Test
    @DisplayName("buscarNotificacoesNaoLidas deve retornar apenas notificacoes nao lidas")
    void buscarNotificacoesNaoLidas_deveRetornarApenasNotificacoesNaoLidas() {
      List<Notificacao> notificacoesNaoLidas = Collections.singletonList(notificacao1);
      Page<Notificacao> pageNotificacoesNaoLidas =
          new PageImpl<>(notificacoesNaoLidas, pageable, 1);

      when(notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataCriacaoDesc(
              usuario1, pageable))
          .thenReturn(pageNotificacoesNaoLidas);
      when(modelMapper.map(notificacao1, NotificacaoDTO.class)).thenReturn(notificacaoDTO1);

      Page<NotificacaoDTO> result =
          notificacaoService.buscarNotificacoesNaoLidas(usuario1, pageable);

      assertEquals(1, result.getContent().size());
      assertEquals(notificacaoDTO1, result.getContent().getFirst());
      verify(notificacaoRepository)
          .findByUsuarioAndLidaFalseOrderByDataCriacaoDesc(usuario1, pageable);
    }
  }

  @Test
  @DisplayName("contarNotificacoesNaoLidas deve retornar contagem correta")
  void contarNotificacoesNaoLidas_deveRetornarContagemCorreta() {
    when(notificacaoRepository.countByUsuarioAndLidaFalse(usuario1)).thenReturn(5L);

    long count = notificacaoService.contarNotificacoesNaoLidas(usuario1);

    assertEquals(5L, count);
    verify(notificacaoRepository).countByUsuarioAndLidaFalse(usuario1);
  }

  @Test
  @DisplayName("marcarTodasComoLidas deve chamar repository com usuario correto")
  void marcarTodasComoLidas_deveChamarRepositoryComUsuarioCorreto() {
    notificacaoService.marcarTodasComoLidas(usuario1);

    verify(notificacaoRepository).marcarTodasComoLidas(usuario1);
  }

  @Nested
  @DisplayName("Testes para criarNotificacaoParaMultiplosUsuarios")
  class CriarNotificacaoParaMultiplosUsuariosTests {

    @Test
    @DisplayName("deve criar notificacoes para todos os usuarios")
    void deveCriarNotificacoesParaTodosOsUsuarios() {
      List<Usuario> destinatarios = Arrays.asList(usuario1, usuario2, usuario3);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(TOTAL_USUARIOS_TESTE, notificacoesSalvas.size());

      for (int i = 0; i < TOTAL_USUARIOS_TESTE; i++) {
        Notificacao notificacao = notificacoesSalvas.get(i);
        Usuario usuarioEsperado = destinatarios.get(i);
        assertNotificacaoBasica(
            notificacao, TITULO_TESTE, DESCRICAO_TESTE, TipoNotificacao.INFO, usuarioEsperado);
      }
    }

    @Test
    @DisplayName("deve filtrar usuarios nulos")
    void deveFiltrarUsuariosNulos() {
      List<Usuario> destinatarios = Arrays.asList(usuario1, null, usuario2);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(2, notificacoesSalvas.size());
      assertEquals(usuario1, notificacoesSalvas.get(0).getUsuario());
      assertEquals(usuario2, notificacoesSalvas.get(1).getUsuario());
    }

    @Test
    @DisplayName("com lista vazia nao deve criar notificacoes")
    void comListaVazia_naoDeveCriarNotificacoes() {
      List<Usuario> destinatarios = Collections.emptyList();

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertTrue(notificacoesSalvas.isEmpty());
    }

    @Test
    @DisplayName("com apenas usuarios nulos nao deve criar notificacoes")
    void comApenasUsuariosNulos_naoDeveCriarNotificacoes() {
      List<Usuario> destinatarios = Arrays.asList(null, null, null);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertTrue(notificacoesSalvas.isEmpty());
    }

    @Test
    @DisplayName("com um unico usuario deve criar uma notificacao")
    void comUmUnicoUsuario_deveCriarUmaNotificacao() {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(1, notificacoesSalvas.size());
      assertNotificacaoBasica(
          notificacoesSalvas.getFirst(),
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          usuario1);
    }

    @Test
    @DisplayName("deve definir data de criacao atual")
    void deveDefinirDataDeCriacaoAtual() {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);
      LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

      Notificacao notificacao = notificacoesSalvas.getFirst();
      assertTrue(notificacao.getDataCriacao().isAfter(antes));
      assertTrue(notificacao.getDataCriacao().isBefore(depois));
    }

    @Test
    @DisplayName("deve configurar todas as notificacoes como nao lidas")
    void deveConfigurarTodasAsNotificacoesComoNaoLidas() {
      List<Usuario> destinatarios = Arrays.asList(usuario1, usuario2);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      notificacoesSalvas.forEach(notificacao -> assertFalse(notificacao.isLida()));
    }

    @Test
    @DisplayName("deve funcionar com referencia id nula")
    void deveFuncionarComReferenciaIdNula() {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          null);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(1, notificacoesSalvas.size());
      assertNull(notificacoesSalvas.getFirst().getReferenciaId());
    }

    @ParameterizedTest
    @EnumSource(TipoNotificacao.class)
    @DisplayName("deve funcionar com todos os tipos de notificacao")
    void deveFuncionarComTodosOsTiposDeNotificacao(TipoNotificacao tipo) {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          tipo,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(1, notificacoesSalvas.size());
      assertEquals(tipo, notificacoesSalvas.getFirst().getTipoNotificacao());
    }

    @ParameterizedTest
    @EnumSource(TipoReferencia.class)
    @DisplayName("deve funcionar com todos os tipos de referencia")
    void deveFuncionarComTodosOsTiposDeReferencia(TipoReferencia tipoReferencia) {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          TITULO_TESTE,
          DESCRICAO_TESTE,
          TipoNotificacao.INFO,
          tipoReferencia,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(1, notificacoesSalvas.size());
      assertEquals(tipoReferencia, notificacoesSalvas.getFirst().getTipoReferencia());
    }

    @ParameterizedTest
    @CsvSource({
      "Título Teste 1, Descrição Teste 1",
      "Título Teste 2, Descrição Teste 2",
      "Título Teste 3, Descrição Teste 3"
    })
    @DisplayName("deve criar notificacao com diferentes dados")
    void deveCriarNotificacaoComDiferentesDados(String titulo, String descricao) {
      List<Usuario> destinatarios = Collections.singletonList(usuario1);

      notificacaoService.criarNotificacaoParaMultiplosUsuarios(
          destinatarios,
          titulo,
          descricao,
          TipoNotificacao.INFO,
          TipoReferencia.PROJETO,
          REFERENCIA_ID_TESTE);

      List<Notificacao> notificacoesSalvas = capturarNotificacoesSalvas();
      assertEquals(1, notificacoesSalvas.size());

      Notificacao notificacao = notificacoesSalvas.getFirst();
      assertEquals(titulo, notificacao.getTitulo());
      assertEquals(descricao, notificacao.getDescricao());
    }
  }
}
