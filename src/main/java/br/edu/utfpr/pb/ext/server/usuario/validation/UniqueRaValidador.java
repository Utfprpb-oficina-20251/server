package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueRa;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueRaValidador implements ConstraintValidator<UniqueRa, String> {
  private final UsuarioRepository usuarioRepository;

  /**
   * Verifica se o número de registro acadêmico (RA) informado é único.
   *
   * Retorna {@code true} se o RA for {@code null}, vazio ou composto apenas por espaços em branco, ou se não existir nenhum usuário cadastrado com esse RA.
   *
   * @param ra número de registro acadêmico a ser validado
   * @return {@code true} se o RA for considerado único ou não informado; {@code false} caso contrário
   */
  @Override
  public boolean isValid(String ra, jakarta.validation.ConstraintValidatorContext context) {
    if (ra == null || ra.trim().isEmpty()) {
      return true;
    }

    return usuarioRepository.findByRegistroAcademico(ra).isEmpty();
  }
}
