package br.edu.utfpr.pb.ext.server.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.EmailOtpAuthRequestDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.SolicitacaoCodigoOTPRequestDTO;
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
  public static final String USUARIO_JA_CADASTRADO = "Usuário já cadastrado";
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

    SolicitacaoCodigoOTPRequestDTO solicitacaoDTO =
        SolicitacaoCodigoOTPRequestDTO.builder().email("testuser@alunos.utfpr.edu.br").build();

    // Cadastrar usuário
    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("testuser@alunos.utfpr.edu.br"));

    // Solicitar código OTP
    mockMvc
        .perform(
            post("/api/auth/solicitar-codigo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitacaoDTO)))
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
                .contentType(MediaType.APPLICATION_JSON)
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
        .andExpect(jsonPath("$.expiresIn").exists())
        .andExpect(jsonPath("$.user.nome").value("testuser")); // Assert only first name
  }

  @Test
  @Transactional
  @DisplayName(
      "Cadastrar um aluno, solicitar código OTP e autenticar com código válido deve retornar informação do aluno")
  void cadastroEAutenticacao_whenCodigoOTPValido_DeveRetornarInformacaoUsuarioAluno()
      throws Exception {
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
                .contentType(MediaType.APPLICATION_JSON)
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
        .andExpect(jsonPath("$.user").exists())
        .andExpect(jsonPath("$.user.email").exists())
        .andExpect(jsonPath("$.user.email").value(email))
        .andExpect(jsonPath("$.user.nome").value("testuser"))
        .andExpect(jsonPath("$.user.authorities").exists())
        .andExpect(jsonPath("$.user.authorities[0]").exists())
        .andExpect(jsonPath("$.user.authorities[0]").value("ROLE_ALUNO"));
  }

  @Test
  @DisplayName("Autenticar com código OTP inválido deve retornar Unprocessable Entity")
  void autenticacao_whenCodigoOTPInvalido_DeveRetornar422() throws Exception {
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
        .andExpect(status().isUnprocessableEntity());
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isConflict())
        .andExpect(status().reason(USUARIO_JA_CADASTRADO));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(status().reason("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br"));
  }

  @Test
  @DisplayName("Solicitar código OTP para um e-mail que não existe deve retornar NotFound")
  void solicitarCodigoOtp_whenEmailNaoExiste_DeveRetornar404() throws Exception {
    SolicitacaoCodigoOTPRequestDTO solicitacaoDTO =
        SolicitacaoCodigoOTPRequestDTO.builder().email("testuser@utfpr.edu.br").build();
    mockMvc
        .perform(
            post("/api/auth/solicitar-codigo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitacaoDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName(
      "Solicitar código OTP para um e-mail que não pertence ao domínio UTFPR deve retornar BadRequest")
  void solicitarCodigoOtp_whenEmailNaoPertenceAoDominioUtfpr_DeveRetornarErroDeValidacao()
      throws Exception {
    SolicitacaoCodigoOTPRequestDTO solicitacaoDTO =
        SolicitacaoCodigoOTPRequestDTO.builder().email("testuser@dominio.com").build();
    mockMvc
        .perform(
            post("/api/auth/solicitar-codigo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(solicitacaoDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation Error"))
        .andExpect(
            jsonPath("$.validationErrors.email")
                .value("E-mail deve ser @utfpr.edu.br ou @alunos.utfpr.edu.br"));
  }
}
