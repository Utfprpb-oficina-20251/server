package br.edu.utfpr.pb.ext.server.usuario;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.auth.dto.RespostaLoginDTO;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioAlunoRequestDTO;
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

  // ... (demais testes inalterados)

  @Test
  void buscarPorEmail_whenUserExists_receiveUser() {
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

    ResponseEntity<UsuarioProjetoDTO[]> response =
        testRestTemplate.getForEntity(
            "/api/usuarios/buscar-email/" + createRequest.getEmail(), UsuarioProjetoDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().length > 0);
    UsuarioProjetoDTO user = response.getBody()[0];
    assertEquals(createRequest.getEmail(), user.getEmail());
    assertEquals(createRequest.getNome(), user.getNome());
  }

  @Test
  void buscarPorEmail_whenUserDoesNotExist_receiveEmptyList() {
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

    String nonExistentEmail = "nonexistent@example.com";

    ResponseEntity<UsuarioProjetoDTO[]> response =
        testRestTemplate.getForEntity(
            "/api/usuarios/buscar-email/" + nonExistentEmail, UsuarioProjetoDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().length);
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
