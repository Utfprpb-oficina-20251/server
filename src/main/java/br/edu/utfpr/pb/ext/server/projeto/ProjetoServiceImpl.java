package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;
 private  final ModelMapper modelMapper;
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

  @Override
  @Transactional
  public ProjetoDTO atualizarProjeto(Long id, ProjetoDTO dto) {

    Projeto projeto = this.projetoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Projeto com ID " + id + " não encontrado."));
    modelMapper.map(dto, projeto);
    Projeto projetoAtualizado = this.save(projeto);

    return modelMapper.map(projetoAtualizado, ProjetoDTO.class);
  }
}
