package br.edu.utfpr.pb.ext.server.usuario.validation;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.validation.anotation.UniqueCpf;
import jakarta.validation.ConstraintValidator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueCpfValidator implements ConstraintValidator<UniqueCpf, String> {
    private final UsuarioRepository usuarioRepository;

    @Override
    public boolean isValid(String cpf, jakarta.validation.ConstraintValidatorContext context) {
        if (usuarioRepository.findByCpf(cpf).isEmpty()) {
            return true; // CPF is not provided, so we skip validation
        }
        return false;
    }
}
