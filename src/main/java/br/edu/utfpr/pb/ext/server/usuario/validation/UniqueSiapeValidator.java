package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueSiape;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueSiapeValidator implements ConstraintValidator<UniqueSiape, String> {
  private final UsuarioRepository usuarioRepository;

  /**
   * Verifica se o valor de siape informado é único no repositório de usuários.
   *
   * @param siape valor do siape a ser validado
   * @param context contexto de validação fornecido pelo framework
   * @return {@code true} se não existir usuário com o siape informado; caso contrário, {@code
   *     false}
   */
  @Override
  public boolean isValid(String siape, jakarta.validation.ConstraintValidatorContext context) {
    return usuarioRepository.findBySiape(siape).isEmpty();
  }
}
