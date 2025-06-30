package br.edu.utfpr.pb.ext.server.notificacao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.notificacao.enums.TipoNotificacao;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @Mock private NotificacaoRepository notificacaoRepository;

  @Mock private ModelMapper modelMapper;

  @InjectMocks private NotificacaoServiceImpl notificacaoService;

  private Usuario usuario1;
  private Usuario usuario2;
  private Notificacao notificacao1;
  private Notificacao notificacao2;
  private NotificacaoDTO notificacaoDTO1;
  private NotificacaoDTO notificacaoDTO2;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // Setup usuarios
    usuario1 = Usuario.builder().id(1L).nome("Usuario 1").email("usuario1@test.com").build();

    usuario2 = Usuario.builder().id(2L).nome("Usuario 2").email("usuario2@test.com").build();

    // Setup notificacoes
    notificacao1 =
        Notificacao.builder()
            .id(1L)
            .titulo("Notificacao 1")
            .descricao("Descrição da notificação 1")
            .tipoNotificacao(TipoNotificacao.INFO)
            .dataCriacao(LocalDateTime.now())
            .lida(false)
            .usuario(usuario1)
            .build();

    notificacao2 =
        Notificacao.builder()
            .id(2L)
            .titulo("Notificacao 2")
            .descricao("Descrição da notificação 2")
            .tipoNotificacao(TipoNotificacao.ALERTA)
            .dataCriacao(LocalDateTime.now())
            .lida(true)
            .usuario(usuario1)
            .build();

    // Setup DTOs
    notificacaoDTO1 =
        NotificacaoDTO.builder()
            .id(1L)
            .titulo("Notificacao 1")
            .descricao("Descrição da notificação 1")
            .tipoNotificacao(TipoNotificacao.INFO)
            .dataCriacao(LocalDateTime.now())
            .lida(false)
            .build();

    notificacaoDTO2 =
        NotificacaoDTO.builder()
            .id(2L)
            .titulo("Notificacao 2")
            .descricao("Descrição da notificação 2")
            .tipoNotificacao(TipoNotificacao.ALERTA)
            .dataCriacao(LocalDateTime.now())
            .lida(true)
            .build();

    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("getRepository deve retornar NotificacaoRepository")
  void getRepository_deveRetornarNotificacaoRepository() {
    assertEquals(notificacaoRepository, notificacaoService.getRepository());
  }

  @Test
  @DisplayName("marcarComoLida com notificacao do usuario deve marcar como lida")
  void marcarComoLida_comNotificacaoDoUsuario_deveMarcarComoLida() {
    // Arrange
    when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao1));
    when(notificacaoRepository.save(notificacao1)).thenReturn(notificacao1);

    // Act
    notificacaoService.marcarComoLida(1L, usuario1);

    // Assert
    assertTrue(notificacao1.isLida());
    verify(notificacaoRepository).save(notificacao1);
  }

  @Test
  @DisplayName("marcarComoLida com notificacao de outro usuario deve lancar exception")
  void marcarComoLida_comNotificacaoDeOutroUsuario_deveLancarException() {
    // Arrange
    when(notificacaoRepository.findById(1L)).thenReturn(Optional.of(notificacao1));

    // Act & Assert
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> notificacaoService.marcarComoLida(1L, usuario2));

    assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
    assertEquals("Usuário não tem permissão para marcar como lida", exception.getReason());
    assertFalse(notificacao1.isLida());
    verify(notificacaoRepository, never()).save(any());
  }

  @Test
  @DisplayName("marcarComoLida com notificacao inexistente deve lancar EntityNotFoundException")
  void marcarComoLida_comNotificacaoInexistente_deveLancarEntityNotFoundException() {
    // Arrange
    when(notificacaoRepository.findById(999L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class, () -> notificacaoService.marcarComoLida(999L, usuario1));

    verify(notificacaoRepository, never()).save(any());
  }

  @Test
  @DisplayName("buscarNotificacoesDoUsuario deve retornar page de NotificacaoDTO")
  void buscarNotificacoesDoUsuario_deveRetornarPageDeNotificacaoDTO() {
    // Arrange
    Page<Notificacao> pageNotificacoes =
        new PageImpl<>(List.of(notificacao1, notificacao2), pageable, 2);
    when(notificacaoRepository.findByUsuarioOrderByDataCriacaoDesc(usuario1, pageable))
        .thenReturn(pageNotificacoes);
    when(modelMapper.map(notificacao1, NotificacaoDTO.class)).thenReturn(notificacaoDTO1);
    when(modelMapper.map(notificacao2, NotificacaoDTO.class)).thenReturn(notificacaoDTO2);

    // Act
    Page<NotificacaoDTO> result =
        notificacaoService.buscarNotificacoesDoUsuario(usuario1, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getContent().size());
    assertEquals(notificacaoDTO1.getId(), result.getContent().get(0).getId());
    assertEquals(notificacaoDTO2.getId(), result.getContent().get(1).getId());
    verify(notificacaoRepository).findByUsuarioOrderByDataCriacaoDesc(usuario1, pageable);
  }

  @Test
  @DisplayName("buscarNotificacoesNaoLidas deve retornar apenas notificacoes nao lidas")
  void buscarNotificacoesNaoLidas_deveRetornarApenasNotificacoesNaoLidas() {
    // Arrange
    Page<Notificacao> pageNotificacoes = new PageImpl<>(List.of(notificacao1), pageable, 1);
    when(notificacaoRepository.findByUsuarioAndLidaFalseOrderByDataCriacaoDesc(usuario1, pageable))
        .thenReturn(pageNotificacoes);
    when(modelMapper.map(notificacao1, NotificacaoDTO.class)).thenReturn(notificacaoDTO1);

    // Act
    Page<NotificacaoDTO> result = notificacaoService.buscarNotificacoesNaoLidas(usuario1, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals(notificacaoDTO1.getId(), result.getContent().getFirst().getId());
    assertFalse(result.getContent().getFirst().isLida());
    verify(notificacaoRepository)
        .findByUsuarioAndLidaFalseOrderByDataCriacaoDesc(usuario1, pageable);
  }

  @Test
  @DisplayName("contarNotificacoesNaoLidas deve retornar contagem correta")
  void contarNotificacoesNaoLidas_deveRetornarContagemCorreta() {
    // Arrange
    when(notificacaoRepository.countByUsuarioAndLidaFalse(usuario1)).thenReturn(5L);

    // Act
    long result = notificacaoService.contarNotificacoesNaoLidas(usuario1);

    // Assert
    assertEquals(5L, result);
    verify(notificacaoRepository).countByUsuarioAndLidaFalse(usuario1);
  }

  @Test
  @DisplayName("marcarTodasComoLidas deve chamar repository com usuario correto")
  void marcarTodasComoLidas_deveChamarRepositoryComUsuarioCorreto() {
    // Arrange
    doNothing().when(notificacaoRepository).marcarTodasComoLidas(usuario1);

    // Act
    notificacaoService.marcarTodasComoLidas(usuario1);

    // Assert
    verify(notificacaoRepository).marcarTodasComoLidas(usuario1);
  }

  @Test
  @DisplayName("buscarNotificacoesDoUsuario com pageable nulo deve funcionar")
  void buscarNotificacoesDoUsuario_comPageableNulo_deveFuncionar() {
    // Arrange
    Page<Notificacao> pageNotificacoes = new PageImpl<>(List.of(notificacao1));
    when(notificacaoRepository.findByUsuarioOrderByDataCriacaoDesc(eq(usuario1), isNull()))
        .thenReturn(pageNotificacoes);
    when(modelMapper.map(notificacao1, NotificacaoDTO.class)).thenReturn(notificacaoDTO1);

    // Act
    Page<NotificacaoDTO> result = notificacaoService.buscarNotificacoesDoUsuario(usuario1, null);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    verify(notificacaoRepository).findByUsuarioOrderByDataCriacaoDesc(eq(usuario1), any());
  }
}
