package br.edu.utfpr.pb.ext.server.usuario.validation.annotation;

import br.edu.utfpr.pb.ext.server.usuario.validation.UniqueCpfValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueCpfValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCpf {
  String message() default "CPF já cadastrado";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
