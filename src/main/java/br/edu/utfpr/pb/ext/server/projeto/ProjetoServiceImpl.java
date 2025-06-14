package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
  private final ProjetoRepository projetoRepository;
  private final ModelMapper modelMapper;
  private final UsuarioRepository usuarioRepository;

  /**
   * Cria uma nova instância do serviço de projetos com o repositório fornecido.
   *
   * @param projetoRepository repositório utilizado para operações de persistência de projetos
   * @param modelMapper mapper para conversão entre objetos
   * @param usuarioRepository repositório para operações com usuários
   */
  public ProjetoServiceImpl(
      ProjetoRepository projetoRepository,
      ModelMapper modelMapper,
      UsuarioRepository usuarioRepository) {
    this.projetoRepository = projetoRepository;
    this.modelMapper = modelMapper;
    this.usuarioRepository = usuarioRepository;
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
  public Projeto preSave(Projeto entity) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      String email = authentication.getName();

      if (entity.getResponsavel() == null) {
        Usuario usuarioAutenticado =
            usuarioRepository
                .findByEmail(email)
                .orElseThrow(
                    () -> new EntityNotFoundException("Usuário autenticado não encontrado"));
        entity.setResponsavel(usuarioAutenticado);
      }
    }
    return super.preSave(entity);
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
