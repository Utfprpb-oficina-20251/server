package br.edu.utfpr.pb.ext.server.notificacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificacaoControllerTest {

  @Mock private NotificacaoService notificacaoService;

  @InjectMocks private NotificacaoController notificacaoController;

  private Usuario usuario;
  private NotificacaoDTO notificacaoDTO1;
  private Page<NotificacaoDTO> pageNotificacoes;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    usuario = Usuario.builder().id(1L).nome("Usuario Teste").email("usuario@test.com").build();

    notificacaoDTO1 =
        NotificacaoDTO.builder()
            .id(1L)
            .titulo("Notificacao 1")
            .descricao("Descrição da notificação 1")
            .tipoNotificacao(TipoNotificacao.INFO)
            .dataCriacao(LocalDateTime.now())
            .lida(false)
            .build();

    NotificacaoDTO notificacaoDTO2 =
        NotificacaoDTO.builder()
            .id(2L)
            .titulo("Notificacao 2")
            .descricao("Descrição da notificação 2")
            .tipoNotificacao(TipoNotificacao.ALERTA)
            .dataCriacao(LocalDateTime.now())
            .lida(true)
            .build();

    pageable = PageRequest.of(0, 10);
    pageNotificacoes = new PageImpl<>(List.of(notificacaoDTO1, notificacaoDTO2), pageable, 2);
  }

  @Test
  @DisplayName("GET /api/notificacao deve retornar page de notificacoes do usuario")
  void listarNotificacoes_deveRetornarPageDeNotificacoesDoUsuario() {
    // Arrange
    when(notificacaoService.buscarNotificacoesDoUsuario(usuario, pageable))
        .thenReturn(pageNotificacoes);

    // Act
    ResponseEntity<Page<NotificacaoDTO>> response =
        notificacaoController.listarNotificacoes(usuario, pageable);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().getContent().size());
    assertEquals(1L, response.getBody().getContent().get(0).getId());
    assertEquals("Notificacao 1", response.getBody().getContent().get(0).getTitulo());
    assertEquals(2L, response.getBody().getContent().get(1).getId());
    assertEquals("Notificacao 2", response.getBody().getContent().get(1).getTitulo());
    assertEquals(2, response.getBody().getTotalElements());
    assertEquals(10, response.getBody().getSize());
    assertEquals(0, response.getBody().getNumber());

    verify(notificacaoService).buscarNotificacoesDoUsuario(usuario, pageable);
  }

  @Test
  @DisplayName("GET /api/notificacao/nao-lidas deve retornar apenas notificacoes nao lidas")
  void listarNotificacoesNaoLidas_deveRetornarApenasNotificacoesNaoLidas() {
    // Arrange
    Page<NotificacaoDTO> pageNaoLidas = new PageImpl<>(List.of(notificacaoDTO1), pageable, 1);
    when(notificacaoService.buscarNotificacoesNaoLidas(usuario, pageable)).thenReturn(pageNaoLidas);

    // Act
    ResponseEntity<Page<NotificacaoDTO>> response =
        notificacaoController.listarNotificacoesNaoLidas(usuario, pageable);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().getContent().size());
    assertEquals(1L, response.getBody().getContent().getFirst().getId());
    assertFalse(response.getBody().getContent().getFirst().isLida());
    assertEquals(1, response.getBody().getTotalElements());

    verify(notificacaoService).buscarNotificacoesNaoLidas(usuario, pageable);
  }

  @Test
  @DisplayName(
      "GET /api/notificacao/count-nao-lidas deve retornar contagem de notificacoes nao lidas")
  void countNotificacoesNaoLidas_deveRetornarContagemDeNotificacoesNaoLidas() {
    // Arrange
    when(notificacaoService.contarNotificacoesNaoLidas(usuario)).thenReturn(5L);

    // Act
    ResponseEntity<Long> response = notificacaoController.countNotificacoesNaoLidas(usuario);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(5L, response.getBody());

    verify(notificacaoService).contarNotificacoesNaoLidas(usuario);
  }

  @Test
  @DisplayName("PUT /api/notificacao/{id}/marcar-lida deve marcar notificacao como lida")
  void marcarComoLida_deveMarcarNotificacaoComoLida() {
    // Arrange
    doNothing().when(notificacaoService).marcarComoLida(1L, usuario);

    // Act
    ResponseEntity<Void> response = notificacaoController.marcarComoLida(1L, usuario);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(notificacaoService).marcarComoLida(1L, usuario);
  }

  @Test
  @DisplayName("PUT /api/notificacao/{id}/marcar-lida com usuario sem permissao deve retornar 422")
  void marcarComoLida_comUsuarioSemPermissao_deveRetornar422() {
    // Arrange
    doThrow(
            new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY, "Usuário não tem permissão"))
        .when(notificacaoService)
        .marcarComoLida(1L, usuario);

    // Act & Assert
    assertThrows(
        ResponseStatusException.class, () -> notificacaoController.marcarComoLida(1L, usuario));

    verify(notificacaoService).marcarComoLida(1L, usuario);
  }

  @Test
  @DisplayName("PUT /api/notificacao/marcar-todas-lidas deve marcar todas como lidas")
  void marcarTodasComoLidas_deveMarcarTodasComoLidas() {
    // Arrange
    doNothing().when(notificacaoService).marcarTodasComoLidas(usuario);

    // Act
    ResponseEntity<Void> response = notificacaoController.marcarTodasComoLidas(usuario);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

    verify(notificacaoService).marcarTodasComoLidas(usuario);
  }

  @Test
  @DisplayName("Deve validar entrada de parâmetros com valores válidos")
  void deveValidarParametrosValidos() {
    // Arrange
    when(notificacaoService.buscarNotificacoesDoUsuario(usuario, pageable))
        .thenReturn(pageNotificacoes);

    // Act
    ResponseEntity<Page<NotificacaoDTO>> response =
        notificacaoController.listarNotificacoes(usuario, pageable);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    verify(notificacaoService).buscarNotificacoesDoUsuario(usuario, pageable);
  }
}
