package br.edu.utfpr.pb.ext.server.sugestaoprojeto.service;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.*;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.*;
import br.edu.utfpr.pb.ext.server.usuario.*;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SugestaoDeProjetoService{

  private final SugestaoDeProjetoRepository repository;
  private final UsuarioRepository usuarioRepository;
  private final UsuarioService usuarioService;

  public SugestaoDeProjetoResponseDTO criar(SugestaoDeProjetoRequestDTO requestDTO) {

    Usuario aluno = usuarioService.obterUsuarioLogado();

    SugestaoDeProjeto sugestao =
        SugestaoDeProjeto.builder()
            .titulo(requestDTO.getTitulo())
            .descricao(requestDTO.getDescricao())
            .publicoAlvo(requestDTO.getPublicoAlvo())
            .aluno(aluno)
            .status(StatusSugestao.AGUARDANDO)
            .build();

    if (requestDTO.getProfessorId() != null) {
      Usuario professor =
          usuarioRepository
              .findById(requestDTO.getProfessorId())
              .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado"));

      if (!professor.isAtivo()
          || professor.getAuthorities().stream()
              .noneMatch(a -> a.getAuthority().equals("ROLE_SERVIDOR"))) {
        throw new IllegalArgumentException("Professor não é servidor ativo");
      }
      sugestao.setProfessor(professor);
    }

    SugestaoDeProjeto sugestaoSalva = repository.save(sugestao);

    return toResponseDTO(sugestaoSalva);
  }

  @PreAuthorize("hasAnyRole('ROLE_SERVIDOR')")
  public List<SugestaoDeProjetoResponseDTO> listarTodos() {
    return repository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .toList();
  }

  public List<SugestaoDeProjetoResponseDTO> listarPorAluno(Long alunoId) {
    return repository.findByAlunoId(alunoId).stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  public List<SugestaoDeProjetoResponseDTO> listarSugestoesDoUsuarioLogado() {
    Usuario usuario = usuarioService.obterUsuarioLogado();
    return repository.findByAlunoId(usuario.getId()).stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  public SugestaoDeProjetoResponseDTO buscarPorId(Long id) {
    return repository
        .findById(id)
        .map(this::toResponseDTO)
        .orElseThrow(() -> new EntityNotFoundException("Sugestão de projeto não encontrada"));
  }

  private SugestaoDeProjetoResponseDTO toResponseDTO(SugestaoDeProjeto sugestao) {
    SugestaoDeProjetoResponseDTO dto = new SugestaoDeProjetoResponseDTO();
    BeanUtils.copyProperties(sugestao, dto);
    return dto;
  }
}
