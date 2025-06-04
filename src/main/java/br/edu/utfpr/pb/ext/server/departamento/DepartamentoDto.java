package br.edu.utfpr.pb.ext.server.departamento;

import lombok.Data;

/**
 * Data Transfer Object (DTO) para a entidade Departamento.
 * Utilizado para transferência de dados entre as camadas da aplicação,
 * especialmente entre o backend e o frontend (ou APIs externas).
 */
@Data
public class DepartamentoDto {

  /**
   * Identificador único do departamento.
   */
  private Long id;

  /**
   * Sigla do departamento (exemplo: DAINF, DAADM).
   */
  private String sigla;

  /**
   * Nome completo do departamento.
   */
  private String nome;
}