package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
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
  private final UsuarioRepository usuarioRepository;
  private final AuthenticationManager authenticationManager;

  /**
   * Realiza o cadastro de um novo usuário com base nos dados fornecidos.
   *
   * @param dto objeto contendo nome, e-mail e registro do usuário a ser cadastrado
   * @return o usuário salvo após o cadastro
   */
  public Usuario cadastro(CadastroUsuarioDTO dto) {
    Usuario usuario =
        Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).cpf(dto.getRegistro()).build();
    return usuarioRepository.save(usuario);
  }

  /**
   * Autentica um usuário com base nas credenciais fornecidas.
   *
   * Caso as credenciais estejam incorretas, lança uma exceção com status HTTP 401.
   * Se o e-mail não estiver cadastrado, lança uma exceção indicando que o e-mail não foi encontrado.
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
