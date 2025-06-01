package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueCpf;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueCpfValidator implements ConstraintValidator<UniqueCpf, String> {
  private final UsuarioRepository usuarioRepository;

  /**
   * Verifica se o CPF fornecido é único no repositório de usuários.
   *
   * @param cpf CPF a ser validado
   * @param context contexto de validação (não utilizado)
   * @return {@code true} se o CPF não estiver cadastrado; {@code false} caso contrário
   */
  @Override
  public boolean isValid(String cpf, jakarta.validation.ConstraintValidatorContext context) {
    return usuarioRepository.findByCpf(cpf).isEmpty();
  }
}
