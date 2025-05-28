package br.edu.utfpr.pb.ext.server.generics;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Objeto de domínio que inclui propriedade ID. Usado como base para demais entidades.
 *
 * @author Rodrigo
 */
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@SuperBuilder
public abstract class BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;
}
