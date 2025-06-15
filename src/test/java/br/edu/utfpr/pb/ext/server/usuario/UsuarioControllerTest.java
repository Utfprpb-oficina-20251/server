package br.edu.utfpr.pb.ext.server.usuario;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioAlunoRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioLogadoInfoDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorRequestDTO;
import br.edu.utfpr.pb.ext.server.usuario.enums.Departamentos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

  private UsuarioServidorRequestDTO createUsuarioServidorRequestDTO() {
    UsuarioServidorRequestDTO request = new UsuarioServidorRequestDTO();
    request.setNome("teste");
    request.setCpf("29212492002");
    request.setSiape("1234567");
    request.setEmail("batata@utfpr.edu.br");
    request.setDepartamento(Departamentos.DAINF);
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
