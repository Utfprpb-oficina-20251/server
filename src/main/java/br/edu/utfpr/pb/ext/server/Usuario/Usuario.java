package br.edu.utfpr.pb.ext.server.Usuario;

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

    @Override
    @Transient
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("Role_USER");
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return nome;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient  public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient  public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient  public boolean isEnabled() {
        return true;
    }
}
