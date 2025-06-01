package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueCpf;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueCpfValidator implements ConstraintValidator<UniqueCpf, String> {
  private final UsuarioRepository usuarioRepository;

  /**
   * Valida se o CPF informado não está cadastrado em nenhum usuário.
   *
   * @param cpf CPF a ser verificado quanto à unicidade
   * @param context contexto de validação (não utilizado)
   * @return {@code true} se o CPF for único; {@code false} se já existir no repositório
   */
  @Override
  public boolean isValid(String cpf, jakarta.validation.ConstraintValidatorContext context) {
    return usuarioRepository.findByCpf(cpf).isEmpty();
  }
}
