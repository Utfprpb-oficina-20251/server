package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario extends BaseEntity implements UserDetails {

  @NotNull private String nome;

  private String registro;

  @NotNull @Email private String email;

  private String telefone;

  @NotNull @ManyToOne
  @JoinColumn(name = "curso_id")
  private Curso curso;

  @Column(name = "ativo")
  private boolean ativo;

  @CreationTimestamp
  @Column(updatable = false, name = "data_criacao")
  private Date dataCriacao;

  @UpdateTimestamp
  @Column(name = "data_atualizacao")
  private Date dataAtualizacao;

  /**
   * Retorna a lista de autoridades concedidas ao usuário, contendo apenas o papel "ROLE_USER".
   *
   * @return coleção de autoridades do usuário
   */
  @Override
  @Transient
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  /**
   * Retorna sempre {@code null}, indicando que a senha do usuário não é armazenada ou gerenciada
   * nesta entidade.
   *
   * @return sempre {@code null}
   */
  @Override
  public String getPassword() {
    // Retornando null intencionalmente pois autenticação será via OTP/JWT
    // e será implementada em tarefa futura
    // senha temporária até a implementação do OTP, significa password
    return "$2a$12$a8kcoUlLHvBlhrEebCYe0uZ2Ofvzijj14HkAfKJmdUGzUCWcUOd7m";
  }

  /**
   * Retorna o email do usuário, utilizado como identificador de login.
   *
   * @return o email do usuário
   */
  @Override
  public String getUsername() {
    return email;
  }

  /**
   * Indica que a conta do usuário nunca expira.
   *
   * @return sempre retorna {@code true}
   */
  @Override
  @Transient
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Indica que a conta do usuário nunca está bloqueada.
   *
   * @return sempre retorna {@code true}
   */
  @Override
  @Transient
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * Indica que as credenciais do usuário nunca expiram.
   *
   * @return sempre retorna {@code true}
   */
  @Override
  @Transient
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indica o estado de ativação da conta do usuário
   *
   * @return boolean indicando o estado de ativação da conta
   */
  @Override
  @Transient
  public boolean isEnabled() {
    return true;
  }
}
