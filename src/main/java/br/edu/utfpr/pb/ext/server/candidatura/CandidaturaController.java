package br.edu.utfpr.pb.ext.server.candidatura;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/candidaturas")
@RequiredArgsConstructor
public class CandidaturaController {

  private final ICandidaturaService candidaturaService;

  @PostMapping("/{projetoId}")
  @Operation(summary = "Candidatar-se a um projeto existente")
  public ResponseEntity<CandidaturaDTO> candidatar(@PathVariable Long projetoId) {
    return ResponseEntity.ok(candidaturaService.candidatar(projetoId));
  }
}
