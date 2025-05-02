package br.edu.utfpr.pb.ext.server.generics;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Objeto de domínio que inclui propriedade ID. Usado como base para demais entidades.
 *
 * @author Rodrigo
 */
@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public boolean isNew() {
    return this.id == null;
  }
}
