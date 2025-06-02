package br.edu.utfpr.pb.ext.server.departamento;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO utilizado para transferir dados da entidade Departamento
 * entre as camadas da aplicação (Controller, Service, etc.).
 */
@Getter
@Setter
public class DepartamentoDto {

  /**
   * Identificador único do departamento.
   */
  private Long id;

  /**
   * Sigla do departamento (ex: DAINF, DAADM).
   */
  private String sigla;

  /**
   * Nome completo do departamento.
   */
  private String nome;

  /**
   * ID do usuário responsável pelo departamento.
   */
  private Long responsavelId;
}