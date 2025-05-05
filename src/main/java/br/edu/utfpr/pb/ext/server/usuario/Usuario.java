package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.generics.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Entity
@Table(name="tb_usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario  extends BaseEntity implements UserDetails{

    @NotNull
    private String nome;

    @NotNull
    private String registro;

    @NotNull
    @Email
    private String email;

    private String telefone;


    @NotNull
    @ManyToOne
    @JoinColumn(name = "curso_id")
    private Curso curso;

    /**
     * Retorna a lista de autoridades concedidas ao usuário, contendo apenas o papel "ROLE_USER".
     *
     * @return coleção de autoridades do usuário
     */
    @Override
    @Transient
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_USER");
    }

    /**
     * Retorna sempre {@code null}, indicando que a senha do usuário não é armazenada ou gerenciada nesta entidade.
     *
     * @return sempre {@code null}
     */
    @Override
    public String getPassword() {
        return null;
    }

    /**
     * Retorna o nome do usuário, utilizado como identificador de login.
     *
     * @return o nome do usuário
     */
    @Override
    public String getUsername() {
        return nome;
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
    @Transient  public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica que as credenciais do usuário nunca expiram.
     *
     * @return sempre retorna {@code true}
     */
    @Override
    @Transient  public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica que a conta do usuário está sempre habilitada.
     *
     * @return sempre retorna {@code true}
     */
    @Override
    @Transient  public boolean isEnabled() {
        return true;
    }
}
