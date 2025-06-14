package br.edu.utfpr.pb.ext.server.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(
    name = "UsuarioLoginDTO",
    description = "Objeto de informação do usuário para ser carregado no contexto do frontend")
public class UsuarioLoginDTO {
  private String email;
  private String nome;
  private Set<String> authorities;
}
