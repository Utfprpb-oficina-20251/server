package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidaturas")
@RequiredArgsConstructor
public class CandidaturaController {

  private final ICandidaturaService candidaturaService;
  private final UsuarioRepository usuarioRepository;

  @PostMapping("/{projetoId}")
  @Operation(summary = "Candidatar-se a um projeto existente")
  public ResponseEntity<CandidaturaDTO> candidatar(
      @PathVariable Long projetoId, Authentication authentication) {
    String email = authentication.getName();

    Usuario aluno =
        usuarioRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));

    return ResponseEntity.ok(candidaturaService.candidatar(projetoId, aluno.getId()));
  }
}
