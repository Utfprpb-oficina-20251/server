package br.edu.utfpr.pb.ext.server.usuario.enums;

import lombok.Getter;

@Getter
public enum Departamentos {
  CALEM("Centro Acadêmico Lingua Estrangeira Moderna"),
  DAADM("Departamento de Administração"),
  DAGRO("Departamento de Agronomia"),
  DACOC("Departamento Acadêmico de Construção Civil"),
  DACON("Departamento de Ciências Contábeis"),
  DAELE("Departamento de Engenharia Elétrica"),
  DAFIS("Departamento de Física"),
  DAHUM("Departamento de Ciências Humanas"),
  DAINF("Departamento de Ciência da Computação"),
  DALET("Departamento de Letras"),
  DAMAT("Departamento de Matemática"),
  DAMEC("Departamento de Engenharia Mecânica"),
  DAQUI("Departamento de Química"),
  LATO_SENSU("Departamento de Pós-Graduação Lato Sensu"),
  NUAPE("Nucleo de Apoio Pedagógico"),
  STRICTO("Departamento de Pós-Graduação Stricto Sensu"),
  ;

  private final String descricao;

  /**
   * Inicializa o enum com a descrição correspondente ao departamento.
   *
   * @param descricao descrição completa do departamento ou unidade acadêmica
   */
  Departamentos(String descricao) {
    this.descricao = descricao;
  }
}
