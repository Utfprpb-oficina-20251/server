package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementação da interface DepartamentoService.
 * Responsável pela regra de negócio da entidade Departamento.
 */
@Service
@RequiredArgsConstructor
public class DepartamentoServiceImpl implements DepartamentoService {

  private final DepartamentoRepository departamentoRepository;
  private final UsuarioRepository usuarioRepository;

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Departamento> findAll() {
    return departamentoRepository.findAll();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Departamento findOne(Long id) {
    return departamentoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Departamento save(Departamento departamento) {
    return departamentoRepository.save(departamento);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(Long id) {
    if (!departamentoRepository.existsById(id)) {
             throw new EntityNotFoundException("Departamento não encontrado com ID: " + id);
        }
    departamentoRepository.deleteById(id);
  }

  /**
   * Associa um usuário existente como responsável por um departamento.
   *
   * @param departamentoId ID do departamento.
   * @param usuarioId      ID do usuário que será responsável.
   */
  @Override
  @Transactional
  public void associarResponsavel(Long departamentoId, Long usuarioId) {
    if (departamentoId == null || usuarioId == null) {
            throw new IllegalArgumentException("IDs do departamento e usuário são obrigatórios");
        }
    Departamento departamento = departamentoRepository.findById(departamentoId)
            .orElseThrow(() -> new EntityNotFoundException("Departamento não encontrado com ID: " + departamentoId));

    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));

    departamento.setResponsavel(usuario);
    departamentoRepository.save(departamento);
  }
}