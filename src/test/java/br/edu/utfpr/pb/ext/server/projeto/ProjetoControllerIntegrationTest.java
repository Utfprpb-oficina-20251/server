package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.curso.CursoRepository;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.util.Date;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjetoControllerIntegrationTest {

  private static final String API_PROJETOS_BUSCAR = "/api/projeto/buscar";

  @Autowired private TestRestTemplate testRestTemplate;

  @Autowired private ProjetoRepository projetoRepository;
  @Autowired private UsuarioRepository usuarioRepository;
  @Autowired private CursoRepository cursoRepository;

  @BeforeEach
  void setUp() {
    projetoRepository.deleteAll();
    usuarioRepository.deleteAll();
    cursoRepository.deleteAll();

    testRestTemplate.getRestTemplate().getInterceptors().clear();
  }

  @AfterEach
  void clearUp() {
    projetoRepository.deleteAll();
    usuarioRepository.deleteAll();
    cursoRepository.deleteAll();

    testRestTemplate.getRestTemplate().getInterceptors().clear();
  }

  @Test
  void buscarProjetos_semFiltros_deveRetornarTodosOsProjetos() {
    criarEsalvarProjeto("Projeto de Robotica", StatusProjeto.EM_ANDAMENTO);
    criarEsalvarProjeto("Projeto de Culinária", StatusProjeto.CONCLUIDO);

    ResponseEntity<ProjetoDTO[]> response =
        testRestTemplate.getForEntity(API_PROJETOS_BUSCAR, ProjetoDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length); // Esperamos 2 projetos de volta
  }

  @Test
  void buscarProjetos_comFiltroDeTitulo_deveRetornarApenasProjetoCorrespondente() {
    // Arrange
    criarEsalvarProjeto("Projeto de Robotica", StatusProjeto.EM_ANDAMENTO);
    criarEsalvarProjeto("Projeto de Culinária", StatusProjeto.CONCLUIDO);

    // Constrói a URL com o query parameter
    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("titulo", "Robotica")
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
        testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals("Projeto de Robotica", response.getBody()[0].getTitulo());
  }

  @Test
  void buscarProjetos_comFiltroDeStatus_deveRetornarApenasProjetoCorrespondente() {
    // Arrange
    criarEsalvarProjeto("Projeto de Robotica", StatusProjeto.EM_ANDAMENTO);
    criarEsalvarProjeto("Projeto de Culinária", StatusProjeto.CONCLUIDO);

    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("status", "CONCLUIDO")
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
        testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals(StatusProjeto.CONCLUIDO, response.getBody()[0].getStatus());
  }

  @Test
  void buscarProjetos_quandoFiltroNaoEncontraResultados_deveRetornarListaVazia() {
    // Arrange
    criarEsalvarProjeto("Projeto de Robótica", StatusProjeto.EM_ANDAMENTO);

    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("titulo", "Inexistente")
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
        testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().length); // A lista deve estar vazia
  }

  private Projeto criarEsalvarProjeto(String titulo, StatusProjeto status) {
    Curso curso =
        cursoRepository.save(
            Curso.builder()
                .nome("Engenharia de Testes")
                .codigo(String.valueOf(System.currentTimeMillis()))
                .build());

    Usuario responsavel =
        usuarioRepository.save(
            Usuario.builder()
                .nome("Usuario " + titulo)
                .email(titulo.replaceAll("\\s", "").toLowerCase() + "@utfpr.edu.br")
                .cpf(String.valueOf(System.nanoTime()))
                .curso(curso)
                .build());

    Projeto projeto =
        Projeto.builder()
            .titulo(titulo)
            .status(status)
            .responsavel(responsavel)
            .descricao("Desc...")
            .justificativa("Just...")
            .dataInicio(new Date())
            .publicoAlvo("Todos")
            .vinculadoDisciplina(false)
            .build();

    return projetoRepository.save(projeto);
  }
}
