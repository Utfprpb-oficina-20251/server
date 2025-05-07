package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UsuarioRepository usuarioRepository;
  private final AuthenticationManager authenticationManager;

  public Usuario cadastro(CadastroUsuarioDTO dto) {
    Usuario usuario = Usuario.builder().nome(dto.getNome()).email(dto.getEmail()).registro(dto.getRegistro()).build();
    return usuarioRepository.save(usuario);
  }

  public Usuario autenticacao(LoginUsuarioDTO dto) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));

    return usuarioRepository.findByEmail(dto.getEmail()).orElseThrow();
  }
}
