package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class DepartamentoServiceImpl extends CrudServiceImpl<Departamento, Long>
    implements DepartamentoService {

  private final DepartamentoRepository departamentoRepository;
  private final UsuarioRepository usuarioRepository;

  public DepartamentoServiceImpl(
      DepartamentoRepository departamentoRepository, UsuarioRepository usuarioRepository) {
    this.departamentoRepository = departamentoRepository;
    this.usuarioRepository = usuarioRepository;
  }

  @Override
  protected JpaRepository<Departamento, Long> getRepository() {
    return departamentoRepository;
  }

  @Override
  protected String getEntityName() {
    return "Departamento";
  }

  @Override
  public void associarResponsavel(Long departamentoId, Long usuarioId) {
    Departamento departamento =
        departamentoRepository
            .findById(departamentoId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        String.format("Departamento não encontrado com ID: %d", departamentoId)));

    Usuario usuario =
        usuarioRepository
            .findById(usuarioId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        String.format("Usuário não encontrado com ID: %d", usuarioId)));

    departamento.setResponsavel(usuario);
    departamentoRepository.save(departamento);
  }
}
