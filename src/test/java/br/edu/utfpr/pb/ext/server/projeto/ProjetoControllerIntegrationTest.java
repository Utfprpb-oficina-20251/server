package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.curso.CursoRepository;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.time.ZoneId;
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

  private Curso cursoDeTeste;
  private Usuario responsavelDeTeste;

  @BeforeEach
  void setUp() {
    projetoRepository.deleteAll();
    usuarioRepository.deleteAll();
    cursoRepository.deleteAll();

    cursoDeTeste = cursoRepository.save(
            Curso.builder().nome("Engenharia de Software de Teste").codigo("SW-TESTE").build());

    responsavelDeTeste = usuarioRepository.save(
            Usuario.builder()
                    .nome("Responsável Padrão")
                    .email("responsavel.padrao@utfpr.edu.br")
                    .cpf(String.valueOf(System.nanoTime()))
                    .curso(cursoDeTeste)
                    .build());

    testRestTemplate.getRestTemplate().getInterceptors().clear();
  }

  // --- TESTES EXISTENTES E FUNCIONAIS ---

  @Test
  void buscarProjetos_semFiltros_deveRetornarTodosOsProjetos() {
    criarEsalvarProjeto("Projeto de Robotica", StatusProjeto.EM_ANDAMENTO);
    criarEsalvarProjeto("Projeto de Culinária", StatusProjeto.CONCLUIDO);

    ResponseEntity<ProjetoDTO[]> response =
            testRestTemplate.getForEntity(API_PROJETOS_BUSCAR, ProjetoDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().length);
  }

  @Test
  void buscarProjetos_comFiltroDeTitulo_deveRetornarApenasProjetoCorrespondente() {
    criarEsalvarProjeto("Projeto de Robotica", StatusProjeto.EM_ANDAMENTO);
    criarEsalvarProjeto("Projeto de Culinária", StatusProjeto.CONCLUIDO);

    String urlComFiltro =
            UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
                    .queryParam("titulo", "Robotica")
                    .toUriString();

    ResponseEntity<ProjetoDTO[]> response =
            testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals("Projeto de Robotica", response.getBody()[0].getTitulo());
  }

  // ... outros testes de filtro de status e "não encontrado" ...


  // --- NOVOS TESTES ADICIONADOS ---

  @Test
  void buscarProjetos_comFiltroDeDataInicioAPartirDe_deveRetornarProjetosCorretos() {
    // Arrange
    Date ontem = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date amanha = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    String hojeString = LocalDate.now().toString(); // Formato YYYY-MM-DD

    criarEsalvarProjeto("Projeto Antigo", StatusProjeto.CONCLUIDO, ontem, responsavelDeTeste);
    criarEsalvarProjeto("Projeto Futuro", StatusProjeto.EM_ANDAMENTO, amanha, responsavelDeTeste);

    String urlComFiltro = UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("dataInicioDe", hojeString)
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
            testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals("Projeto Futuro", response.getBody()[0].getTitulo());
  }

  @Test
  void buscarProjetos_comFiltroDeDataInicioAte_deveRetornarProjetosCorretos() {
    // Arrange
    Date ontem = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date amanha = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    String hojeString = LocalDate.now().toString();

    criarEsalvarProjeto("Projeto Antigo", StatusProjeto.CONCLUIDO, ontem, responsavelDeTeste);
    criarEsalvarProjeto("Projeto Futuro", StatusProjeto.EM_ANDAMENTO, amanha, responsavelDeTeste);

    String urlComFiltro = UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("dataInicioAte", hojeString)
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
            testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals("Projeto Antigo", response.getBody()[0].getTitulo());
  }

  @Test
  void buscarProjetos_comFiltroDeIdCurso_deveRetornarProjetosDoCursoCorreto() {
    // Arrange
    // O cursoDeTeste e responsavelDeTeste (Eng. de Software) já foram criados no setUp.
    // Vamos criar um segundo curso e responsável para o cenário de teste.
    Curso cursoDesign = cursoRepository.save(Curso.builder().nome("Design Gráfico").codigo("DG").build());
    Usuario responsavelDesign = usuarioRepository.save(Usuario.builder().nome("Beto do Design").email("beto@utfpr.edu.br").cpf("2222").curso(cursoDesign).build());

    // Cria um projeto para cada responsável/curso
    criarEsalvarProjeto("Projeto de Software", StatusProjeto.EM_ANDAMENTO, new Date(), responsavelDeTeste);
    criarEsalvarProjeto("Projeto de Design", StatusProjeto.EM_ANDAMENTO, new Date(), responsavelDesign);

    String urlComFiltro = UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
            .queryParam("idCurso", cursoDeTeste.getId()) // Filtra pelo ID do primeiro curso
            .toUriString();

    // Act
    ResponseEntity<ProjetoDTO[]> response =
            testRestTemplate.getForEntity(urlComFiltro, ProjetoDTO[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().length);
    assertEquals("Projeto de Software", response.getBody()[0].getTitulo());
  }


  // --- MÉTODOS AUXILIARES ATUALIZADOS ---

  private Projeto criarEsalvarProjeto(String titulo, StatusProjeto status) {
    // Chama a versão mais completa com valores padrão
    return criarEsalvarProjeto(titulo, status, new Date(), this.responsavelDeTeste);
  }

  private Projeto criarEsalvarProjeto(String titulo, StatusProjeto status, Date dataInicio, Usuario responsavel) {
    Projeto projeto =
            Projeto.builder()
                    .titulo(titulo)
                    .status(status)
                    .responsavel(responsavel) // Usa o responsável passado como parâmetro
                    .descricao("Desc...")
                    .justificativa("Just...")
                    .dataInicio(dataInicio) // Usa a data passada como parâmetro
                    .publicoAlvo("Todos")
                    .vinculadoDisciplina(false)
                    .build();

    return projetoRepository.save(projeto);
  }
}