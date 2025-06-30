package br.edu.utfpr.pb.ext.server.notificacao;

import br.edu.utfpr.pb.ext.server.notificacao.dto.NotificacaoDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificacao")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notificacao", description = "Serviços relacionados a notificação do usuário logado")
public class NotificacaoController {
  private final NotificacaoService service;

  @GetMapping
  @Operation(
      summary = "Listar todas as notificações",
      description =
          "Retorna uma lista paginada com todas as notificações do usuário autenticado, ordenadas por data de criação (mais recentes primeiro)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de notificações retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
      })
  public ResponseEntity<Page<NotificacaoDTO>> listarNotificacoes(
      @AuthenticationPrincipal Usuario usuario, @ParameterObject Pageable pageable) {
    Page<NotificacaoDTO> notificacoes = service.buscarNotificacoesDoUsuario(usuario, pageable);
    return ResponseEntity.ok(notificacoes);
  }

  @GetMapping("/nao-lidas")
  @Operation(
      summary = "Listar notificações não lidas",
      description =
          "Retorna uma lista paginada apenas com as notificações não lidas do usuário autenticado, ordenadas por data de criação (mais recentes primeiro)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de notificações não lidas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
      })
  public ResponseEntity<Page<NotificacaoDTO>> listarNotificacoesNaoLidas(
      @AuthenticationPrincipal Usuario usuario, @ParameterObject Pageable pageable) {
    Page<NotificacaoDTO> notificacoes = service.buscarNotificacoesNaoLidas(usuario, pageable);
    return ResponseEntity.ok(notificacoes);
  }

  @GetMapping("/count-nao-lidas")
  @Operation(
      summary = "Contar notificações não lidas",
      description = "Retorna o número total de notificações não lidas do usuário autenticado")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Contagem de notificações não lidas retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
      })
  public ResponseEntity<Long> countNotificacoesNaoLidas(@AuthenticationPrincipal Usuario usuario) {
    long count = service.contarNotificacoesNaoLidas(usuario);
    return ResponseEntity.ok(count);
  }

  @PutMapping("/{id}/marcar-lida")
  @Operation(
      summary = "Marcar notificação como lida",
      description =
          "Marca uma notificação específica como lida. Somente o proprietário da notificação pode executar esta ação")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Notificação marcada como lida com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
        @ApiResponse(responseCode = "404", description = "Notificação não encontrada"),
        @ApiResponse(
            responseCode = "422",
            description = "Usuário não tem permissão para marcar esta notificação como lida")
      })
  public ResponseEntity<Void> marcarComoLida(
      @Parameter(description = "ID da notificação a ser marcada como lida", example = "1")
          @PathVariable
          @Positive Long id,
      @AuthenticationPrincipal Usuario usuario) {
    service.marcarComoLida(id, usuario);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/marcar-todas-lidas")
  @Operation(
      summary = "Marcar todas as notificações como lidas",
      description =
          "Marca todas as notificações não lidas do usuário autenticado como lidas em uma única operação")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Todas as notificações foram marcadas como lidas com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
      })
  public ResponseEntity<Void> marcarTodasComoLidas(@AuthenticationPrincipal Usuario usuario) {
    service.marcarTodasComoLidas(usuario);
    return ResponseEntity.noContent().build();
  }
}
