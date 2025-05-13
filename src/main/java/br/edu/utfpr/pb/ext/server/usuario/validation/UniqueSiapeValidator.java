package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueSiape;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueSiapeValidator implements ConstraintValidator<UniqueSiape, String> {
  private final UsuarioRepository usuarioRepository;

  @Override
  public boolean isValid(String siape, jakarta.validation.ConstraintValidatorContext context) {
    return usuarioRepository.findBySiape(siape).isEmpty();
  }
}
