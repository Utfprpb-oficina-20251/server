package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidade que representa um Departamento da instituição.
 * Cada departamento possui uma sigla única e um nome.
 */
@Entity
@Table(name = "tb_departamento") // Nome da tabela no banco de dados
@Getter
@Setter
public class Departamento extends BaseEntity {

  /**
   * Sigla do departamento (ex: DAINF, DAADM).
   * Deve ser única e conter no máximo 20 caracteres.
   */
  @Column(nullable = false, unique = true, length = 20)
  private String sigla;

  /**
   * Nome completo do departamento.
   * Campo obrigatório.
   */
  @Column(nullable = false)
  private String nome;
}