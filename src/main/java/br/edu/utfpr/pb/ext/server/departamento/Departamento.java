package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade que representa a associação de um responsável a um departamento. Cada departamento pode
 * ter apenas um responsável cadastrado.
 */
@Entity
@Table(name = "tb_departamento")
@Getter
@Setter
@NoArgsConstructor
public class Departamento extends BaseEntity {

  /**
   * Departamento ao qual o responsável está associado. O valor é único para garantir que cada
   * departamento tenha apenas um responsável.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true)
  private Departamentos departamento;

  /**
   * Usuário responsável pelo departamento. Relacionamento muitos-para-um com a entidade {@link
   * Usuario}.
   */
  @ManyToOne(optional = false)
  @JoinColumn(name = "usuario_id")
  private Usuario responsavel;
}
