package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueRa;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueRaValidador implements ConstraintValidator<UniqueRa, String> {
  private final UsuarioRepository usuarioRepository;

  /**
   * Valida se o número de registro acadêmico (RA) informado é único entre os usuários cadastrados.
   *
   * Retorna {@code true} se o RA for {@code null}, vazio, composto apenas por espaços em branco ou não existir nenhum usuário com esse RA; caso contrário, retorna {@code false}.
   *
   * @param ra número de registro acadêmico a ser verificado
   * @return {@code true} se o RA for único ou não informado; {@code false} se já existir um usuário com esse RA
   */
  @Override
  public boolean isValid(String ra, jakarta.validation.ConstraintValidatorContext context) {
    if (ra == null || ra.trim().isEmpty()) {
      return true;
    }

    return usuarioRepository.findByRegistroAcademico(ra).isEmpty();
  }
}
