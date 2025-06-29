package br.edu.utfpr.pb.ext.server.candidatura;

import br.edu.utfpr.pb.ext.server.projeto.IProjetoService;
import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.IUsuarioService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CandidaturaServiceImpl implements ICandidaturaService {

  private final CandidaturaRepository candidaturaRepository;
  private final IUsuarioService usuarioService;
  private final IProjetoService projetoService;
  private final ModelMapper modelMapper;

  @Override
  public CandidaturaDTO candidatar(Long projetoId) {
    Projeto projeto = projetoService.findOne(projetoId);
    Usuario aluno = usuarioService.obterUsuarioLogado();

    if (!StatusProjeto.EM_ANDAMENTO.equals(projeto.getStatus())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Projeto não está aberto para candidaturas");
    }

    if (candidaturaRepository.existsByProjetoIdAndAlunoId(projetoId, aluno.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Você já está inscrito neste projeto");
    }

    long totalCandidaturas = candidaturaRepository.countByProjetoId(projetoId);
    if (projeto.getQtdeVagas() != null && totalCandidaturas >= projeto.getQtdeVagas()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vagas preenchidas");
    }

    Candidatura candidatura =
        Candidatura.builder()
            .projeto(projeto)
            .aluno(aluno)
            .dataCandidatura(LocalDateTime.now())
            .build();

    return modelMapper.map(candidaturaRepository.save(candidatura), CandidaturaDTO.class);
  }
}
