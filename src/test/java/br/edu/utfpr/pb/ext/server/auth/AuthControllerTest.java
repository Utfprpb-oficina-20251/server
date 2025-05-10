package br.edu.utfpr.pb.ext.server.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.utfpr.pb.ext.server.auth.dto.CadastroUsuarioDTO;
import br.edu.utfpr.pb.ext.server.auth.dto.LoginUsuarioDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @Transactional
  @Description("Cadastrar um usuário com sucesso e autenticar com ele deve retornar um token JWT")
  void cadastro_whenNovoUsuario_UsuarioDeveConseguirAutenticar() throws Exception {
    LoginUsuarioDTO loginUsuarioDTO =
        LoginUsuarioDTO.builder().email("testuser@alunos.utfpr.edu.br").senha("password").build();
    CadastroUsuarioDTO cadastroDTO =
        CadastroUsuarioDTO.builder()
            .nome("testuser")
            .email("testuser@alunos.utfpr.edu.br")
            .registro("A1234567")
            .build();

    mockMvc
        .perform(
            post("/api/auth/cadastro")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(cadastroDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("testuser@alunos.utfpr.edu.br"));

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginUsuarioDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.expiresIn").exists());
  }

  @Test
  @Description("Autenticar com credencial inválida deve retornar Unauthorized")
  void autenticacao_whenEnviarUsuarioComSenhaInvalida_DeveRetornar401() throws Exception {
    LoginUsuarioDTO loginUsuarioDTO =
        LoginUsuarioDTO.builder().email("testuser@alunos.utfpr.edu.br").senha("abublé").build();

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUsuarioDTO)))
        .andExpect(status().isUnauthorized());
  }
}
