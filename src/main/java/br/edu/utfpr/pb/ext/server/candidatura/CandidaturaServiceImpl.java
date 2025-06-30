package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.event.EventPublisher;
import br.edu.utfpr.pb.ext.server.projeto.IProjetoService;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CandidaturaServiceImpl implements ICandidaturaService {

  private final CandidaturaRepository candidaturaRepository;
  private final IUsuarioService usuarioService;
  private final IProjetoService projetoService;
  private final EventPublisher eventPublisher;

  @Override
  @Transactional
  public Candidatura candidatar(Long projetoId) {
    Projeto projeto = projetoService.findOne(projetoId);
    Usuario aluno = usuarioService.obterUsuarioLogado();

    if (!StatusProjeto.EM_ANDAMENTO.equals(projeto.getStatus())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Projeto não está aberto para candidaturas");
    }

    Candidatura candidatura =
        candidaturaRepository.findByProjetoIdAndAlunoId(projetoId, aluno.getId()).orElse(null);

    if (candidatura != null) {
      if (StatusCandidatura.APROVADA.equals(candidatura.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Sua candidatura já foi aprovada para este projeto");
      }

      if (StatusCandidatura.REJEITADA.equals(candidatura.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Sua candidatura foi rejeitada para este projeto");
      }

      if (StatusCandidatura.PENDENTE.equals(candidatura.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Você já está inscrito neste projeto");
      }

      candidatura.setStatus(StatusCandidatura.PENDENTE);
      candidatura.setDataCandidatura(LocalDateTime.now());
    } else {
      candidatura =
          Candidatura.builder()
              .projeto(projeto)
              .aluno(aluno)
              .status(StatusCandidatura.PENDENTE)
              .dataCandidatura(LocalDateTime.now())
              .build();
    }

    Candidatura candidaturaBD = candidaturaRepository.save(candidatura);
    eventPublisher.publishCandidaturaCriada(candidaturaBD);
    return candidaturaBD;
  }

  @Override
  @Transactional
  public void atualizarStatusCandidaturas(List<Candidatura> candidaturas) {
    if (candidaturas == null || candidaturas.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lista de candidaturas vazia");
    }

    for (Candidatura candidatura : candidaturas) {
      Candidatura candidaturaBD =
          candidaturaRepository
              .findById(candidatura.getId())
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND,
                          "Candidatura com ID " + candidatura.getId() + " não encontrada"));

      if (StatusCandidatura.APROVADA.equals(candidaturaBD.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Candidatura " + candidatura.getId() + " já foi aprovada");
      }

      if (StatusCandidatura.REJEITADA.equals(candidaturaBD.getStatus())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Candidatura " + candidatura.getId() + " já foi rejeitada");
      }

      candidaturaBD.setStatus(candidatura.getStatus());
      candidaturaRepository.save(candidaturaBD);
      eventPublisher.publishCandidaturaAtualizada(candidaturaBD);
    }
  }

  @Override
  public List<Candidatura> findAllByAlunoId(Long alunoId) {
    return candidaturaRepository.findAllByAlunoId(alunoId).orElse(List.of());
  }

  @Override
  public List<Candidatura> findAllPendentesByProjetoId(Long projetoId) {
    return candidaturaRepository
        .findAllByProjetoIdAndStatus(projetoId, StatusCandidatura.PENDENTE)
        .orElse(List.of());
  }

  @Override
  public Candidatura findById(Long id) {
    return candidaturaRepository
        .findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Candidatura com ID " + id + " não encontrada"));
  }
}
