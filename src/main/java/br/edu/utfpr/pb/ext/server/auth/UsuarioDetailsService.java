package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Serviço personalizado para carregar detalhes do usuário baseado no email. Este serviço é
 * utilizado exclusivamente pelo EmailOtpAuthenticationProvider e serviços que necessitam de um
 * UserDetails
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {
  private final UsuarioRepository repository;

  public UsuarioDetailsService(UsuarioRepository repository) {
    this.repository = repository;
  }

  /**
   * Carrega os detalhes do usuário baseado no email fornecido.
   *
   * @param email o email do usuário
   * @return UserDetails do usuário encontrado
   * @throws UsernameNotFoundException se o usuário não for encontrado
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return repository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
  }
}
