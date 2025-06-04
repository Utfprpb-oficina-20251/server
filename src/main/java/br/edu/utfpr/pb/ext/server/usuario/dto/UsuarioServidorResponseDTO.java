package br.edu.utfpr.pb.ext.server.usuario.dto;

import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
import lombok.Data;

@Data
public class UsuarioServidorResponseDTO {

  private Long id;

  private String nome;

  private String email;

  private String telefone;

  private String enderecoCompleto;

  private Departamentos departamento;
}
