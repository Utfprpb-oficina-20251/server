package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
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
            .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));
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
    Departamento departamento = departamentoRepository.findById(departamentoId)
            .orElseThrow(() -> new RuntimeException("Departamento não encontrado"));

    Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

    departamento.setResponsavel(usuario);
    departamentoRepository.save(departamento);
  }
}