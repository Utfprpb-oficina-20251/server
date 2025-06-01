package br.edu.utfpr.pb.ext.server.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.EmailOtpAuthRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthTestConfig.class)
class AuthControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @Transactional
  @DisplayName(
      "Cadastrar um usuário com sucesso e solicitar código OTP deve retornar mensagem de sucesso")
  void cadastro_whenNovoUsuario_DeveConseguirSolicitarCodigoOTP() throws Exception {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@alunos.utfpr.edu.br")
            .registro("12345678901")
            .build();

    // Cadastrar usuário
    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("testuser@alunos.utfpr.edu.br"));

    // Solicitar código OTP
    mockMvc
        .perform(post("/api/auth/solicitar-codigo").param("email", "testuser@alunos.utfpr.edu.br"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mensagem").exists());
  }

  @Test
  @Transactional
  @DisplayName(
      "Cadastrar um usuário, solicitar código OTP e autenticar com código válido deve retornar token JWT")
  void cadastroEAutenticacao_whenCodigoOTPValido_DeveRetornarTokenJWT() throws Exception {
    // Arrange
    String email = "testuser@alunos.utfpr.edu.br";
    String code = "123456";

    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder().nome("testuser").email(email).registro("12345678901").build();

    EmailOtpAuthRequestDTO authRequestDTO =
        EmailOtpAuthRequestDTO.builder().email(email).code(code).build();

    // Cadastrar usuário
    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email));

    // Autenticar com código OTP
    mockMvc
        .perform(
            post("/api/auth/login-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.expiresIn").exists());
  }

  @Test
  @DisplayName("Autenticar com código OTP inválido deve retornar Unauthorized")
  void autenticacao_whenCodigoOTPInvalido_DeveRetornar401() throws Exception {
    EmailOtpAuthRequestDTO authRequestDTO =
        EmailOtpAuthRequestDTO.builder()
            .email("testuser@alunos.utfpr.edu.br")
            .code("codigo-invalido")
            .build();

    mockMvc
        .perform(
            post("/api/auth/login-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequestDTO)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Transactional
  @DisplayName("Cadastrar um usuário que já foi cadastrado deve retornar Conflict")
  void cadastro_whenUsuarioJaCadastrado_DeveRetornar409() throws Exception {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@alunos.utfpr.edu.br")
            .registro("12345678901")
            .build();

    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isConflict())
        .andExpect(status().reason("Usuário já cadastrado"));
  }

  @Test
  @Transactional
  @DisplayName("Cadastrar um usuário com e-mail sem domínio UTFPR deve retornar BadRequest")
  void cadastro_whenEmailNaoTemDominioUTFPR_DeveRetornar400() throws Exception {
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@dominio.com")
            .registro("12345678901")
            .build();

    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br"));
  }

  @Test
  @DisplayName("Solicitar código OTP para um e-mail que não existe deve retornar NotFound")
  void solicitarCodigoOtp_whenEmailNaoExiste_DeveRetornar404() throws Exception {
    mockMvc
        .perform(post("/api/auth/solicitar-codigo").param("email", "testuser@dominio.com"))
        .andExpect(status().isNotFound());
  }
}
