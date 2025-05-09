package br.edu.utfpr.pb.ext.server.usuario.validation.anotation;

import br.edu.utfpr.pb.ext.server.usuario.validation.UniqueSiapeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueSiapeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueSiape {
    String message() default "SIAPE já cadastrado";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
