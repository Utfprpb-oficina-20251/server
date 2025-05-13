package br.edu.utfpr.pb.ext.server.usuario.dto;

import lombok.Data;

@Data
public class UsuarioServidorResponseDTO {

  private Long id;

  private String nomeCompleto;

  private String emailInstitucional;

  private String telefone;

  private String enderecoCompleto;

  private String departamento;
}
