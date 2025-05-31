package br.edu.utfpr.pb.ext.server.usuario.authority;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "tb_authority")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authority implements GrantedAuthority {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  @NotBlank(message = "O nome da permissão é obrigatório.")
  @Size(max = 50, message = "O nome da permissão deve ter no máximo 50 caracteres.")
  private String authority;
}
