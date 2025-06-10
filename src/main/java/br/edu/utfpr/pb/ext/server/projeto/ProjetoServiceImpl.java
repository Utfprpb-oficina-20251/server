package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;

import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;
  private final ModelMapper modelMapper;

  /**
   * Cria uma nova instância do serviço de projetos com o repositório fornecido.
   *
   * @param projetoRepository repositório utilizado para operações de persistência de projetos
   */
  public ProjetoServiceImpl(ProjetoRepository projetoRepository, ModelMapper modelMapper) {
    this.projetoRepository = projetoRepository;
    this.modelMapper = modelMapper;
  }

  /**
   * Retorna o repositório JPA utilizado para operações CRUD com a entidade Projeto.
   *
   * @return o repositório ProjetoRepository associado à entidade Projeto
   */
  @Override
  protected JpaRepository<Projeto, Long> getRepository() {
    return projetoRepository;
  }


  /**
   * Cancela um projeto existente, alterando seu status para {@code CANCELADO} e registrando uma
   * justificativa.
   *
   * <p>Somente o responsável principal pelo projeto está autorizado a realizar essa operação.
   *
   * @param id o identificador do projeto a ser cancelado
   * @param dto objeto contendo a justificativa do cancelamento
   * @param usuarioId identificador do usuário que está tentando realizar o cancelamento
   * @throws ResponseStatusException se o projeto não for encontrado (404) ou se o usuário não for o
   *     responsável (403)
   */
  @Override
  public void cancelar(Long id, CancelamentoProjetoDTO dto, Long usuarioId) {
      if (dto.getJustificativa() == null || dto.getJustificativa().trim().isEmpty()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A justificativa é obrigatória.");
      }
      Projeto projeto =
              projetoRepository
                      .findById(id)
                      .orElseThrow(
                              () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado"));
      // Verifica se o projeto já está cancelado
      if (projeto.getStatus() == StatusProjeto.CANCELADO) {
          throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Projeto já está cancelado");
      }

      // Verifica se existe equipe executora
      if (projeto.getEquipeExecutora() == null || projeto.getEquipeExecutora().isEmpty()) {
          throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, "Projeto não possui equipe executora definida");
      }
      // Aqui assumimos o primeiro da equipe como responsável principal
      boolean isResponsavelPrincipal =
              projeto.getEquipeExecutora().stream()
                      .findFirst()
                      .map(u -> u.getId().equals(usuarioId))
                      .orElse(false);

      if (!isResponsavelPrincipal) {
          throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, "Apenas o responsável principal pode cancelar o projeto.");
      }

      projeto.setStatus(StatusProjeto.CANCELADO);
      projeto.setJustificativaCancelamento(dto.getJustificativa());

      projetoRepository.save(projeto);
  }

  @Override
  @Transactional
  public ProjetoDTO atualizarProjeto(Long id, ProjetoDTO dto) {

    Projeto projeto =
        this.projetoRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Projeto com ID " + id + " não encontrado."));
    modelMapper.map(dto, projeto);
    Projeto projetoAtualizado = this.save(projeto);

    return modelMapper.map(projetoAtualizado, ProjetoDTO.class);

  }
}
