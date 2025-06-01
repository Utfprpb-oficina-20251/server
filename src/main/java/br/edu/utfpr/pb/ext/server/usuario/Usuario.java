package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.br.CPF;
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

  @NotNull @CPF private String cpf;

  private String siape;

  @Column(name = "registro_academico")
  private String registroAcademico;

  @NotNull @Email private String email;

  private String telefone;

  @NotNull @ManyToOne
  @JoinColumn(name = "curso_id")
  private Curso curso;

  private Departamentos departamento;

  @Column(name = "ativo")
  private boolean ativo;

  @CreationTimestamp
  @Column(updatable = false, name = "data_criacao")
  private Date dataCriacao;

  @UpdateTimestamp
  @Column(name = "data_atualizacao")
  private Date dataAtualizacao;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {
        CascadeType.DETACH, CascadeType.MERGE,
        CascadeType.PERSIST, CascadeType.REFRESH
      })
  @JoinTable(
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "authority_id"))
  private Set<Authority> authorities;

  /**
   * Retorna as permissões associadas ao usuário.
   *
   * @return um novo conjunto contendo as autoridades do usuário
   */
  @Override
  @Transient
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new HashSet<>(authorities);
  }

  /**
   * Retorna sempre null, pois a autenticação do usuário é feita exclusivamente por OTP ou JWT, sem armazenamento ou uso de senha.
   *
   * @return null, indicando ausência de senha para autenticação.
   */
  @Override
  public String getPassword() {
    // Retornando null intencionalmente pois autenticação é via OTP/JWT
    return null;
  }

  /**
   * Retorna o email do usuário, utilizado como nome de usuário para autenticação no sistema.
   *
   * @return o email do usuário
   */
  @Override
  public String getUsername() {
    return email;
  }

  /**
   * Indica que a conta do usuário está sempre ativa e nunca expira.
   *
   * @return sempre {@code true}, indicando que a conta não expira
   */
  @Override
  @Transient
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Indica que a conta do usuário está sempre desbloqueada.
   *
   * @return sempre {@code true}, indicando que a conta nunca é considerada bloqueada
   */
  @Override
  @Transient
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * Indica que as credenciais do usuário nunca expiram.
   *
   * @return sempre retorna {@code true}, sinalizando que as credenciais estão permanentemente válidas
   */
  @Override
  @Transient
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indica se a conta do usuário está habilitada.
   *
   * @return sempre retorna {@code true}, indicando que a conta está habilitada, independentemente do estado real do usuário.
   */
  @Override
  @Transient
  public boolean isEnabled() {
    return true;
  }
}
