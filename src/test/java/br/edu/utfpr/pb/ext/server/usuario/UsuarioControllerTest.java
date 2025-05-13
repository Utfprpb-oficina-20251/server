package br.edu.utfpr.pb.ext.server.usuario;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
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
    request.setEmailInstitucional("invalid-email");

    ResponseEntity<Object> response =
        testRestTemplate.postForEntity(API_USERS, request, Object.class);

    assertEquals(400, response.getStatusCode().value());
  }

  private UsuarioServidorRequestDTO createUsuarioServidorRequestDTO() {
    UsuarioServidorRequestDTO request = new UsuarioServidorRequestDTO();
    request.setNomeCompleto("teste");
    request.setCpf("29212492002");
    request.setSiape("1234567");
    request.setEmailInstitucional("batata@utfpr.edu.br");
    request.setDepartamento(Departamentos.DAINF);
    return request;
  }
}
