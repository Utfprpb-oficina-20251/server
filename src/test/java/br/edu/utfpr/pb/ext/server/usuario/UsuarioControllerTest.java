package br.edu.utfpr.pb.ext.server.usuario;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.departamento.Departamento;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioAlunoRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioLogadoInfoDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsuarioControllerTest {

  private static final String API_USERS = "/api/usuarios/servidor";
  private static final String API_USERS_ALUNO = "/api/usuarios/aluno";

  @Autowired private TestRestTemplate testRestTemplate;

  @Autowired private UsuarioRepository usuarioRepository;

  @BeforeEach
  void cleanUp() {
    usuarioRepository.deleteAll();
    testRestTemplate.getRestTemplate().getInterceptors().clear();
  }

  @Test
  void postUser_whenUserIsValidAndComplete_receiveOk() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    request.setTelefone("46999999999");
    request.setEnderecoCompleto("Rua Teste");

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(200, response.getStatusCode().value());
  }

  @Test
  void postUser_whenUserIsValid_receiveOkAndToken() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();

    ResponseEntity<RespostaLoginDTO> response =
        testRestTemplate.postForEntity(API_USERS, request, RespostaLoginDTO.class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getToken());
    assertTrue(response.getBody().getExpiresIn() > 0);
  }

  @Test
  void postUser_whenUserIsInvalidCpf_receiveBadRequest() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    request.setCpf("invalid-cpf");

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUser_WhenUserCpfAlreadyExists_receiveBadRequest() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    testRestTemplate.postForEntity(API_USERS, request, Object.class);

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUser_whenUserIsInvalidSiape_receiveBadRequest() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    request.setSiape("invalid-siape");

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUser_WhenUserSiapeAlreadyExists_receiveBadRequest() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    testRestTemplate.postForEntity(API_USERS, request, Object.class);

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUser_whenUserIsInvalidEmail_receiveBadRequest() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    request.setEmail("invalid-email");

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUserAluno_whenUserIsValid_receiveOk() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);

    assertEquals(200, response.getStatusCode().value());
  }

  @Test
  void postUserAluno_whenUserIsValid_receiveOkAndToken() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();

    ResponseEntity<RespostaLoginDTO> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, RespostaLoginDTO.class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertNotNull(response.getBody().getToken());
    assertTrue(response.getBody().getExpiresIn() > 0);
  }

  @Test
  void postUserAluno_whenUserIsInvalidCpf_receiveBadRequest() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();
    request.setCpf("invalid-cpf");
    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUserAluno_WhenUserCpfAlreadyExists_receiveBadRequest() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();
    testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUserAluno_whenRegistroAcademicoAlreadyExists_receiveBadRequest() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();
    testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void postUserAluno_whenUserIsInvalidEmail_receiveBadRequest() {
    UsuarioAlunoRequestDTO request = createUsuarioAlunoRequestDTO();
    request.setEmail("invalid-email");
    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS_ALUNO, request, Object.class);
    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void getMeuPerfil_whenUserIsAuthenticated_receiveUserProfile() {
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, request, RespostaLoginDTO.class);

    assertEquals(200, loginResponse.getStatusCode().value());
    assertNotNull(loginResponse.getBody());
    assertNotNull(loginResponse.getBody().getToken());

    String token = loginResponse.getBody().getToken();

    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    ResponseEntity<UsuarioLogadoInfoDTO> response =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(request.getEmail(), response.getBody().getEmail());
    assertEquals(request.getNome(), response.getBody().getNome());
  }

  @Test
  void getMeuPerfil_whenUserIsUnauthenticated_receiveUnauthorized() {
    testRestTemplate.getRestTemplate().getInterceptors().clear();

    ResponseEntity<Object> response =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", Object.class);

    assertEquals(403, response.getStatusCode().value());
  }

  @Test
  void getAllUsers_whenUnauthenticated_receiveForbidden() {
    // Ensure no authentication headers are present
    testRestTemplate.getRestTemplate().getInterceptors().clear();

    ResponseEntity<Object> response =
        testRestTemplate.getForEntity("/api/usuarios/executores", Object.class);

    assertEquals(403, response.getStatusCode().value());
  }

  @Test
  void getAllUsers_whenAuthenticated_receiveListOfUsers() {
    // Create a user and authenticate
    UsuarioServidorRequestDTO request = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, request, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();

    // Add authentication header for subsequent requests
    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    // Call the endpoint
    ResponseEntity<UsuarioProjetoDTO[]> response =
        testRestTemplate.getForEntity("/api/usuarios/executores", UsuarioProjetoDTO[].class);

    // Verify response
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);

    // Verify user data
    boolean foundFirstUser = false;

    for (UsuarioProjetoDTO user : response.getBody()) {
      if (user.getEmail().equals(request.getEmail())) {
        foundFirstUser = true;
        assertEquals(request.getNome(), user.getNome());
      }
    }

    assertTrue(foundFirstUser);
  }

  @Test
  void getAllProfessors_whenUnauthenticated_receiveForbidden() {
    testRestTemplate.getRestTemplate().getInterceptors().clear();

    ResponseEntity<Object> response =
        testRestTemplate.getForEntity("/api/usuarios/professores", Object.class);

    assertEquals(403, response.getStatusCode().value());
  }

  @Test
  void getAllProfessors_whenAuthenticated_receiveOnlyUsersWithUtfprEmail() {
    UsuarioServidorRequestDTO utfprUser = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, utfprUser, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();

    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    ResponseEntity<UsuarioProjetoDTO[]> response =
        testRestTemplate.getForEntity("/api/usuarios/professores", UsuarioProjetoDTO[].class);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());

    for (UsuarioProjetoDTO user : response.getBody()) {
      assertTrue(
          user.getEmail().endsWith("@utfpr.edu.br"),
          "Only users with @utfpr.edu.br emails should be returned");
    }

    assertEquals(1, response.getBody().length);
    assertEquals(utfprUser.getEmail(), response.getBody()[0].getEmail());
  }

  @Test
  void updateMeuPerfil_whenUserIsAuthenticated_receiveUpdatedProfile() {
    UsuarioServidorRequestDTO createRequest = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, createRequest, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();

    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    ResponseEntity<UsuarioLogadoInfoDTO> currentProfile =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    UsuarioLogadoInfoDTO updateRequest = currentProfile.getBody();
    updateRequest.setNome("Nome Atualizado");
    updateRequest.setDepartamento(1L);

    // Update profile
    ResponseEntity<UsuarioLogadoInfoDTO> response =
        testRestTemplate.exchange(
            "/api/usuarios/meu-perfil",
            org.springframework.http.HttpMethod.PUT,
            new org.springframework.http.HttpEntity<>(updateRequest),
            UsuarioLogadoInfoDTO.class);

    // Verify response
    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("Nome Atualizado", response.getBody().getNome());
    assertEquals(1L, response.getBody().getDepartamento());
  }

  @Test
  void updateMeuPerfil_whenUserIsUnauthenticated_receiveForbidden() {
    testRestTemplate.getRestTemplate().getInterceptors().clear();

    UsuarioLogadoInfoDTO updateRequest = new UsuarioLogadoInfoDTO();
    updateRequest.setNome("Nome Qualquer");

    ResponseEntity<UsuarioLogadoInfoDTO> response =
        testRestTemplate.exchange(
            "/api/usuarios/meu-perfil",
            org.springframework.http.HttpMethod.PUT,
            new org.springframework.http.HttpEntity<>(updateRequest),
            UsuarioLogadoInfoDTO.class);

    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
  }

  @Test
  void updateMeuPerfil_whenNameIsNull_receiveBadRequest() {
    UsuarioServidorRequestDTO createRequest = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, createRequest, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();

    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    UsuarioLogadoInfoDTO updateRequest = new UsuarioLogadoInfoDTO();
    updateRequest.setNome(null);

    ResponseEntity<Object> response =
        testRestTemplate.exchange(
            "/api/usuarios/meu-perfil",
            org.springframework.http.HttpMethod.PUT,
            new org.springframework.http.HttpEntity<>(updateRequest),
            Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  @Test
  void updateMeuPerfil_verifyPersistence_afterUpdate() {
    UsuarioServidorRequestDTO createRequest = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS, createRequest, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();

    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    ResponseEntity<UsuarioLogadoInfoDTO> currentProfile =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    UsuarioLogadoInfoDTO updateRequest = currentProfile.getBody();
    updateRequest.setNome("Novo Nome");

    testRestTemplate.exchange(
        "/api/usuarios/meu-perfil",
        org.springframework.http.HttpMethod.PUT,
        new org.springframework.http.HttpEntity<>(updateRequest),
        UsuarioLogadoInfoDTO.class);

    ResponseEntity<UsuarioLogadoInfoDTO> afterUpdate =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    assertEquals(200, afterUpdate.getStatusCode().value());
    assertNotNull(afterUpdate.getBody());
    assertEquals("Novo Nome", afterUpdate.getBody().getNome());
  }

  @Test
  void updateMeuPerfil_whenCursoIsNull_shouldNotUpdateCurso() {
    // Create and authenticate user
    UsuarioAlunoRequestDTO createRequest = createUsuarioAlunoRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
        testRestTemplate.postForEntity(API_USERS_ALUNO, createRequest, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();
    testRestTemplate
        .getRestTemplate()
        .getInterceptors()
        .add(
            (httpRequest, bytes, execution) -> {
              httpRequest.getHeaders().add("Authorization", "Bearer " + token);
              return execution.execute(httpRequest, bytes);
            });

    // Get current profile
    ResponseEntity<UsuarioLogadoInfoDTO> currentProfile =
        testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    // Set curso to null explicitly
    UsuarioLogadoInfoDTO updateRequest = currentProfile.getBody();
    updateRequest.setNome("Nome Atualizado");
    updateRequest.setCurso(null);

    // Update profile
    ResponseEntity<UsuarioLogadoInfoDTO> response =
        testRestTemplate.exchange(
            "/api/usuarios/meu-perfil",
            org.springframework.http.HttpMethod.PUT,
            new org.springframework.http.HttpEntity<>(updateRequest),
            UsuarioLogadoInfoDTO.class);

    // Verify response
    assertEquals(200, response.getStatusCode().value());
    assertEquals("Nome Atualizado", response.getBody().getNome());
    // Curso should remain unchanged
    assertEquals(currentProfile.getBody().getCurso(), response.getBody().getCurso());
  }

  @Test
  void updateMeuPerfil_whenDepartamentoIsNull_shouldNotUpdateDepartamento() {
    UsuarioServidorRequestDTO createRequest = createUsuarioServidorRequestDTO();
    ResponseEntity<RespostaLoginDTO> loginResponse =
            testRestTemplate.postForEntity(API_USERS, createRequest, RespostaLoginDTO.class);

    String token = loginResponse.getBody().getToken();
    testRestTemplate
            .getRestTemplate()
            .getInterceptors()
            .add(
                    (httpRequest, bytes, execution) -> {
                      httpRequest.getHeaders().add("Authorization", "Bearer " + token);
                      return execution.execute(httpRequest, bytes);
                    });

    ResponseEntity<UsuarioLogadoInfoDTO> currentProfile =
            testRestTemplate.getForEntity("/api/usuarios/meu-perfil", UsuarioLogadoInfoDTO.class);

    Long originalDepartamentoId = currentProfile.getBody().getDepartamento();
    assertNotNull(originalDepartamentoId, "Original department should not be null");

    UsuarioLogadoInfoDTO updateRequest = currentProfile.getBody();
    updateRequest.setNome("Nome Atualizado");
    updateRequest.setDepartamento(null);

    ResponseEntity<UsuarioLogadoInfoDTO> response =
            testRestTemplate.exchange(
                    "/api/usuarios/meu-perfil",
                    org.springframework.http.HttpMethod.PUT,
                    new org.springframework.http.HttpEntity<>(updateRequest),
                    UsuarioLogadoInfoDTO.class);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Nome Atualizado", response.getBody().getNome());
    assertNotNull(response.getBody().getDepartamento(), "Department should not be null after update");
    assertEquals(originalDepartamentoId, response.getBody().getDepartamento(), "Department should remain unchanged when null is provided");
  }

  private UsuarioServidorRequestDTO createUsuarioServidorRequestDTO() {
    UsuarioServidorRequestDTO request = new UsuarioServidorRequestDTO();
    request.setNome("teste");
    request.setCpf("29212492002");
    request.setSiape("1234567");
    request.setEmail("batata@utfpr.edu.br");
    request.setDepartamentoId(1L);
    return request;
  }

  private UsuarioAlunoRequestDTO createUsuarioAlunoRequestDTO() {
    UsuarioAlunoRequestDTO request = new UsuarioAlunoRequestDTO();
    request.setNome("teste");
    request.setCpf("29212492002");
    request.setRegistroAcademico("1234567");
    request.setEmail("batata@alunos.utfpr.edu.br");
    return request;
  }
}
