package br.edu.utfpr.pb.ext.server.auth.dto;

import lombok.Data;

@Data
public class LoginUsuarioDTO {
  private String email;
  private String senha;
}
