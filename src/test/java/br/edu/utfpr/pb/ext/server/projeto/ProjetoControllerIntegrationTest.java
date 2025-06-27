package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.curso.CursoRepository;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import br.edu.utfpr.pb.ext.server.usuario.authority.Authority;
import br.edu.utfpr.pb.ext.server.usuario.authority.AuthorityRepository;
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
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProjetoControllerIntegrationTest {

  private static final String API_PROJETOS_BUSCAR = "/api/projeto/buscar";
  private static final String API_PROJETOS_ALUNOS_EXECUTORES = "/api/projeto/alunosexecutores"; // <-- NOVO

  @Autowired private TestRestTemplate testRestTemplate;
  @Autowired private ProjetoRepository projetoRepository;
  @Autowired private UsuarioRepository usuarioRepository;
  @Autowired private CursoRepository cursoRepository;
  @Autowired private AuthorityRepository authorityRepository;

  private Curso cursoDeTeste;
  private Usuario responsavelDeTeste;
  private Usuario alunoDeTeste;

  @BeforeEach
  void setUp() {
    projetoRepository.deleteAll();
    usuarioRepository.deleteAll();
    cursoRepository.deleteAll();
    testRestTemplate.getRestTemplate().getInterceptors().clear();

    cursoDeTeste =
        cursoRepository.save(
            Curso.builder().nome("Engenharia de Software de Teste").codigo("SW-TESTE").build());

    responsavelDeTeste =
        usuarioRepository.save(
            Usuario.builder()
                .nome("Responsável Padrão")
                .email("responsavel.padrao@utfpr.edu.br")
                .cpf(String.valueOf(System.nanoTime()))
                .curso(cursoDeTeste)
                .build());
  }

  @AfterEach
  void cleanUp() {

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
    Date ontem =
        Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date amanha =
        Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    String hojeString = LocalDate.now().toString(); // Formato YYYY-MM-DD

    criarEsalvarProjeto("Projeto Antigo", StatusProjeto.CONCLUIDO, ontem, responsavelDeTeste);
    criarEsalvarProjeto("Projeto Futuro", StatusProjeto.EM_ANDAMENTO, amanha, responsavelDeTeste);

    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
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
    Date ontem =
        Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date amanha =
        Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    String hojeString = LocalDate.now().toString();

    criarEsalvarProjeto("Projeto Antigo", StatusProjeto.CONCLUIDO, ontem, responsavelDeTeste);
    criarEsalvarProjeto("Projeto Futuro", StatusProjeto.EM_ANDAMENTO, amanha, responsavelDeTeste);

    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
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
    Curso cursoDesign =
        cursoRepository.save(Curso.builder().nome("Design Gráfico").codigo("DG").build());
    Usuario responsavelDesign =
        usuarioRepository.save(
            Usuario.builder()
                .nome("Beto do Design")
                .email("beto@utfpr.edu.br")
                .cpf("2222")
                .curso(cursoDesign)
                .build());

    // Cria um projeto para cada responsável/curso
    criarEsalvarProjeto(
        "Projeto de Software", StatusProjeto.EM_ANDAMENTO, new Date(), responsavelDeTeste);
    criarEsalvarProjeto(
        "Projeto de Design", StatusProjeto.EM_ANDAMENTO, new Date(), responsavelDesign);

    String urlComFiltro =
        UriComponentsBuilder.fromPath(API_PROJETOS_BUSCAR)
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

  @Test
  void buscarAlunosExecutores_comIdsDeProjetosValidos_deveRetornarListaDeAlunosFormatada() {
    // Arrange

    Authority managedAuthorityAluno = authorityRepository.findByAuthority("ROLE_ALUNO")
            .orElseThrow(() -> new IllegalStateException("Authority 'ROLE_ALUNO' não encontrada no banco de dados."));
    Authority managedAuthorityProfessor = authorityRepository.findByAuthority("ROLE_SERVIDOR")
            .orElseThrow(() -> new IllegalStateException("Authority 'ROLE_PROFESSOR' não encontrada no banco de dados."));

    // O curso e o responsável ainda precisam ser gerenciados, mas eles são criados neste teste.
    Curso managedCurso = cursoRepository.findById(this.cursoDeTeste.getId()).get();
    Usuario managedResponsavel = usuarioRepository.findById(this.responsavelDeTeste.getId()).get();

    // 1. Criar usuários com diferentes perfis (alunos e professores) usando as entidades gerenciadas
    Usuario alunoExecutor1 = this.usuarioRepository.save(Usuario.builder().nome("Ana Aluna").email("ana@alunos.utfpr.edu.br").cpf("111").authorities(Set.of(managedAuthorityAluno)).curso(managedCurso).build());
    Usuario alunoExecutor2 = usuarioRepository.save(Usuario.builder().nome("Beto Aluno").email("beto@alunos.utfpr.edu.br").cpf("222").authorities(Set.of(managedAuthorityAluno)).curso(managedCurso).build());
    Usuario professorExecutor = usuarioRepository.save(Usuario.builder().nome("Carlos Professor").email("carlos@prof.utfpr.edu.br").cpf("333").authorities(Set.of(managedAuthorityProfessor)).curso(managedCurso).build());

    // 2. Criar projetos e adicionar as equipes executoras, usando o responsável gerenciado
    Projeto projetoX = criarEsalvarProjeto("Projeto X", StatusProjeto.EM_ANDAMENTO, new Date(), managedResponsavel);
    projetoX.setEquipeExecutora(List.of(alunoExecutor1, professorExecutor));
    projetoRepository.save(projetoX);

    Projeto projetoY = criarEsalvarProjeto("Projeto Y", StatusProjeto.EM_ANDAMENTO, new Date(), managedResponsavel);
    projetoY.setEquipeExecutora(List.of(alunoExecutor1, alunoExecutor2));
    projetoRepository.save(projetoY);

    Projeto projetoZ = criarEsalvarProjeto("Projeto Z - Sem Alunos", StatusProjeto.CONCLUIDO, new Date(), managedResponsavel);
    projetoZ.setEquipeExecutora(List.of(professorExecutor));
    projetoRepository.save(projetoZ);


    // 3. Montar a URL para o endpoint, solicitando os projetos X e Y
    String url = UriComponentsBuilder.fromPath(API_PROJETOS_ALUNOS_EXECUTORES)
            .queryParam("idsProjeto", projetoX.getId() + "," + projetoY.getId())
            .toUriString();

    // Act
    ResponseEntity<String[]> response = testRestTemplate.getForEntity(url, String[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());

    List<String> resultado = Arrays.asList(response.getBody());

    assertEquals(3, resultado.size());

    assertThat(resultado).containsExactlyInAnyOrder(
            "Ana Aluna-ana@alunos.utfpr.edu.br-Projeto X",
            "Ana Aluna-ana@alunos.utfpr.edu.br-Projeto Y",
            "Beto Aluno-beto@alunos.utfpr.edu.br-Projeto Y"
    );
    assertThat(resultado).noneMatch(s -> s.contains("Carlos Professor"));
  }

  @Test
  void buscarAlunosExecutores_comIdDeProjetoInexistente_deveRetornarListaVazia() {
    // Arrange
    long idInexistente = 999L;
    String url = UriComponentsBuilder.fromPath(API_PROJETOS_ALUNOS_EXECUTORES)
            .queryParam("idsProjeto", idInexistente)
            .toUriString();

    // Act
    ResponseEntity<String[]> response = testRestTemplate.getForEntity(url, String[].class);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(0, response.getBody().length);
  }

  // --- MÉTODOS AUXILIARES ATUALIZADOS ---

  private Projeto criarEsalvarProjeto(String titulo, StatusProjeto status) {
    // Chama a versão mais completa com valores padrão
    return criarEsalvarProjeto(titulo, status, new Date(), this.responsavelDeTeste);
  }

  private Projeto criarEsalvarProjeto(
      String titulo, StatusProjeto status, Date dataInicio, Usuario responsavel) {
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
