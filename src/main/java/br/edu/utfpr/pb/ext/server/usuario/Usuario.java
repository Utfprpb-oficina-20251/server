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
   * Retorna as autoridades associadas ao usuário.
   *
   * @return conjunto de autoridades concedidas ao usuário
   */
  @Override
  @Transient
  @JsonIgnore
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new HashSet<>(authorities);
  }

  /**
   * Retorna sempre null, pois a autenticação do usuário é realizada exclusivamente por OTP ou JWT, sem uso de senha.
   *
   * @return sempre null, indicando que não há senha armazenada ou utilizada para autenticação.
   */
  @Override
  public String getPassword() {
    // Retornando null intencionalmente pois autenticação é via OTP/JWT
    return null;
  }

  /**
   * Retorna o email do usuário para autenticação.
   *
   * @return o email cadastrado do usuário
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
   * Indica que as credenciais do usuário estão sempre válidas e não expiram.
   *
   * @return sempre retorna {@code true}
   */
  @Override
  @Transient
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indica se a conta do usuário está habilitada.
   *
   * @return sempre retorna {@code true}, indicando que a conta está habilitada independentemente do
   *     estado real de ativação.
   */
  @Override
  @Transient
  public boolean isEnabled() {
    return true;
  }
}
