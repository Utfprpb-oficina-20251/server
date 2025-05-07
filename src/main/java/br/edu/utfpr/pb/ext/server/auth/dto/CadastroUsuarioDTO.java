package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.Data;

@Data
public class CadastroUsuarioDTO {
  private String email;
  private String nome;
  private String registro;
}
