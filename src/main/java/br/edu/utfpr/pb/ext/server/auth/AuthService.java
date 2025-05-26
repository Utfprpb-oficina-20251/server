package br.edu.utfpr.pb.ext.server.auth;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.EmailOtpAuthRequestDTO;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationProvider;
import br.edu.utfpr.pb.ext.server.auth.otp.EmailOtpAuthenticationToken;
import br.edu.utfpr.pb.ext.server.email.impl.EmailServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
  public static final String ALUNOS_UTFPR_EDU_BR = "@alunos.utfpr.edu.br";
  public static final String UTFPR_EDU_BR = "@utfpr.edu.br";
  private final UsuarioRepository usuarioRepository;
  private final AuthorityRepository authorityRepository;
  private final EmailServiceImpl emailService;
  private final EmailOtpAuthenticationProvider emailOtpAuthenticationProvider;

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
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao cadastrar"));
    authorities.add(authority);
    usuario.setAuthorities(authorities);
    return usuarioRepository.save(usuario);
  }

  /**
   * Solicita o envio de um código OTP para o email do usuário.
   * 
   * @param email Email do usuário
   * @return true se o código foi enviado com sucesso
   */
  public boolean solicitarCodigoOtp(String email) {
    try {
      // Verificar se o usuário existe
      usuarioRepository.findByEmail(email)
          .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));

      emailService.generateAndSendCode(email, "autenticacao");
      return true;
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao enviar código de verificação");
    }
  }

  /**
   * Autentica um usuário usando o código OTP.
   * 
   * @param dto DTO contendo email e código OTP
   * @return O usuário autenticado
   */
  public Usuario autenticacaoOtp(EmailOtpAuthRequestDTO dto) {
    try {
      EmailOtpAuthenticationToken authToken =
          new EmailOtpAuthenticationToken(dto.getEmail(), dto.getCode());
      Authentication authentication = 
          emailOtpAuthenticationProvider.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return usuarioRepository.findByEmail(dto.getEmail())
          .orElseThrow(() -> new UsernameNotFoundException("Email não cadastrado"));
    } catch (BadCredentialsException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido ou expirado");
    }
  }
}
