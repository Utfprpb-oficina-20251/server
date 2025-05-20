package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
  public static final String ALUNOS_UTFPR_EDU_BR = "@alunos.utfpr.edu.br";
  public static final String UTFPR_EDU_BR = "@utfpr.edu.br";
  private final UsuarioRepository usuarioRepository;
  private final AuthenticationManager authenticationManager;
  private final AuthorityRepository authorityRepository;

  /**
   * Realiza o cadastro de um novo usuário com base nos dados fornecidos.
   *
   * @param dto objeto contendo nome, e-mail e registro do usuário a ser cadastrado
   * @return o usuário salvo após o cadastro
   */
  public Usuario cadastro(CadastroUsuarioDTO dto) {
    Usuario usuario =
        Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).cpf(dto.getRegistro()).build();
    Set<Authority> authorities = new HashSet<>();

    final String ROLE_ALUNO = "ROLE_ALUNO";
    final String ROLE_SERVIDOR = "ROLE_SERVIDOR";

    String email = dto.getEmail();
    String authorityName;

    // verifica dominio do email para definir nivel de permissão
    if (email.endsWith(UTFPR_EDU_BR)) {
      authorityName = ROLE_SERVIDOR;
    } else if (email.endsWith(ALUNOS_UTFPR_EDU_BR)) {
      authorityName = ROLE_ALUNO;
    } else {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br");
    }
    Authority authority =
        authorityRepository
            .findByAuthority(authorityName)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao cadastrar"));
    authorities.add(authority);
    usuario.setAuthorities(authorities);
    return usuarioRepository.save(usuario);
  }

  /**
   * Autentica um usuário com base nas credenciais fornecidas.
   *
   * <p>Caso as credenciais estejam incorretas, lança uma exceção com status HTTP 401. Se o e-mail
   * não estiver cadastrado, lança uma exceção indicando que o e-mail não foi encontrado.
   *
   * @param dto objeto contendo e-mail e senha para autenticação
   * @return o usuário autenticado
   */
  public Usuario autenticacao(LoginUsuarioDTO dto) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));
    } catch (BadCredentialsException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
    }

    return usuarioRepository
        .findByEmail(dto.getEmail())
        .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));
  }
}
