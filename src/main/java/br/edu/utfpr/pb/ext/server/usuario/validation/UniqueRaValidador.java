package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.annotation.UniqueRa;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueRaValidador implements ConstraintValidator<UniqueRa, String> {
  private final UsuarioRepository usuarioRepository;

  @Override
  public boolean isValid(String ra, jakarta.validation.ConstraintValidatorContext context) {
    if (ra == null || ra.trim().isEmpty()) {
      return true;
    }

    return usuarioRepository.findByRegistroAcademico(ra).isEmpty();
  }
}
