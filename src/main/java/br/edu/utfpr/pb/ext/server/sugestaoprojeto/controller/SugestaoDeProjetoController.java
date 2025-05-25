package br.edu.utfpr.pb.ext.server.sugestaoprojeto.controller;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoRequestDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoResponseDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoService;
import br.edu.utfpr.pb.ext.server.usuario.*;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestao")
@RequiredArgsConstructor
public class SugestaoDeProjetoController {

  private final SugestaoDeProjetoService service;
  private final UsuarioRepository usuarioRepository;

  @PostMapping
  public ResponseEntity<SugestaoDeProjetoResponseDTO> criar(
      @Valid @RequestBody SugestaoDeProjetoRequestDTO requestDTO) {
    SugestaoDeProjetoResponseDTO responseDTO = service.criar(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
  }

  @GetMapping("/minhas-sugestoes")
  public ResponseEntity<List<SugestaoDeProjetoResponseDTO>> listarSugestoesDoUsuarioLogado() {
    List<SugestaoDeProjetoResponseDTO> sugestoes = service.listarSugestoesDoUsuarioLogado();
    return ResponseEntity.ok(sugestoes);
  }

  @GetMapping
  public ResponseEntity<List<SugestaoDeProjetoResponseDTO>> listarTodas() {
    List<SugestaoDeProjetoResponseDTO> sugestoes = service.listarTodos();
    return ResponseEntity.ok(sugestoes);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SugestaoDeProjetoResponseDTO> buscarPorId(@PathVariable Long id) {
    SugestaoDeProjetoResponseDTO sugestao = service.buscarPorId(id);
    return ResponseEntity.ok(sugestao);
  }
}
