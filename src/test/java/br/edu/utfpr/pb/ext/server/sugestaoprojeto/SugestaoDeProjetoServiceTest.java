package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.*;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import br.edu.utfpr.pb.ext.server.usuario.Role;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SugestaoDeProjetoServiceTest {

  @Mock private SugestaoDeProjetoRepository repository;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private UsuarioServiceImpl usuarioService;

  @InjectMocks private SugestaoDeProjetoService service;

  // --- TESTE 1: Criar sugestão com dados válidos ---
  @Test
  void criarSugestao_ComDadosValidos_DeveRetornarDTO() {
    // 1. Configuração
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .titulo("Projeto Teste")
            .descricao("Esta descrição tem mais do que 30 caracteres obrigatórios")
            .publicoAlvo("Alunos")
            .build();

    Usuario aluno =
        Usuario.builder()
            .nome("Aluno Teste")
            .email("aluno@utfpr.edu.br")
            .cpf("12345678901")
            .ativo(true)
            .build();

    aluno.setId(1L);

    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);

    // Mock da entidade salva (USANDO BUILDER)
    SugestaoDeProjeto sugestaoSalva =
        SugestaoDeProjeto.builder()
            .id(1L) // ID explícito
            .titulo(request.getTitulo())
            .aluno(aluno)
            .build();

    when(repository.save(any())).thenReturn(sugestaoSalva);

    // 2. Execução
    SugestaoDeProjetoResponseDTO response = service.criar(request);

    // 3. Verificações
    assertEquals(1L, response.getId());
    assertEquals("Projeto Teste", response.getTitulo());
  }

  // --- TESTE 2: Criar sugestão com professor inválido ---
  @Test
  void criarSugestao_ComProfessorInvalido_DeveLancarExcecao() {
    // 1. Configuração
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .professorId(999L) // ID inválido
            .build();

    when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

    // 2. Execução + Verificação
    assertThrows(EntityNotFoundException.class, () -> service.criar(request));
  }

  @Test
  void criarSugestao_ComProfessorInativo_DeveLancarExcecao() {
    // 1. Configuração do request
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .titulo("Título válido")
            .descricao("Descrição com mais de 30 caracteres...")
            .publicoAlvo("Alunos")
            .professorId(1L)
            .build();

    // 2. Mock do professor INATIVO
    Usuario professorInativo =
        Usuario.builder()
            .nome("Professor Teste")
            .email("professor@utfpr.edu.br")
            .cpf("12345678901")
            .ativo(false)
            .build();

    professorInativo.setId(1L);

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(professorInativo));

    // 3. Execução + Verificação
    assertThrows(
        IllegalArgumentException.class,
        () -> service.criar(request),
        "Deveria lançar exceção para professor inativo");
  }

  @Test
  void criarSugestao_DeveTerStatusAguardandoPorPadrao() {
    // 1. Configuração do request
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .titulo("Título válido")
            .descricao("Descrição com mais de 30 caracteres...")
            .publicoAlvo("Alunos")
            .build();

    // 2. Mock do aluno autenticado (ADICIONE ESSA PARTE!)
    Usuario aluno = new Usuario();
    aluno.setId(1L);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);

    // 3. Mock do repositório para capturar o objeto salvo
    SugestaoDeProjeto sugestaoSalva = new SugestaoDeProjeto();
    when(repository.save(any()))
        .thenAnswer(
            invocation -> {
              SugestaoDeProjeto s = invocation.getArgument(0);
              sugestaoSalva.setStatus(s.getStatus()); // Captura o status
              return s;
            });

    // 4. Execução
    service.criar(request);

    // 5. Verificação
    assertEquals(
        StatusSugestao.AGUARDANDO, sugestaoSalva.getStatus(), "Status inicial deve ser AGUARDANDO");
  }

  @Test
  void criarSugestao_SemProfessor_DeveSalvarComProfessorNull() {
    // 1. Configuração - Request SEM professorId
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .titulo("Projeto sem professor")
            .descricao("Descrição válida com mais de 30 caracteres...")
            .publicoAlvo("Alunos")
            .build(); // Sem .professorId()

    // 2. Mock do repositório para capturar o objeto salvo
    SugestaoDeProjeto sugestaoSalva = new SugestaoDeProjeto();
    when(repository.save(any()))
        .thenAnswer(
            invocation -> {
              SugestaoDeProjeto s = invocation.getArgument(0);
              sugestaoSalva.setId(1L);
              sugestaoSalva.setTitulo(s.getTitulo());
              sugestaoSalva.setProfessor(s.getProfessor()); // Captura o professor (deve ser null)
              return sugestaoSalva;
            });

    // 3. Execução
    Usuario aluno = new Usuario();
    aluno.setId(1L);
    when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
    SugestaoDeProjetoResponseDTO response = service.criar(request);

    // 4. Verificações
    assertNull(sugestaoSalva.getProfessor(), "Professor deve ser null quando não informado");
    assertEquals("Projeto sem professor", response.getTitulo());
  }
// --- TESTES ADICIONAIS DE CRIAÇÃO ---
  @Test
  void criarSugestao_ComTodosOsCampos_DeveMappearCorretamenteNoResponseDTO() {
      // Given
      SugestaoDeProjetoRequestDTO requestDTO = SugestaoDeProjetoRequestDTO.builder()
          .titulo("Projeto de IA")
          .descricao("Sistema de inteligência artificial para análise de dados")
          .publicoAlvo("Desenvolvedores e cientistas de dados")
          .professorId(2L)
          .build();

      Usuario aluno = new Usuario();
      aluno.setId(1L);
      aluno.setNome("João Silva");

      Usuario professor = new Usuario();
      professor.setId(2L);
      professor.setNome("Dr. Maria Santos");
      professor.setAtivo(true);
      professor.setRoles(Set.of(new Role("ROLE_SERVIDOR")));

      LocalDateTime agora = LocalDateTime.now();
      SugestaoDeProjeto sugestaoSalva = new SugestaoDeProjeto();
      sugestaoSalva.setId(3L);
      sugestaoSalva.setTitulo(requestDTO.getTitulo());
      sugestaoSalva.setDescricao(requestDTO.getDescricao());
      sugestaoSalva.setPublicoAlvo(requestDTO.getPublicoAlvo());
      sugestaoSalva.setAluno(aluno);
      sugestaoSalva.setProfessor(professor);
      sugestaoSalva.setStatus(StatusSugestao.AGUARDANDO);
      sugestaoSalva.setDataCriacao(agora);

      when(usuarioService.obterUsuarioLogado()).thenReturn(aluno);
      when(usuarioRepository.findById(2L)).thenReturn(Optional.of(professor));
      when(repository.save(any(SugestaoDeProjeto.class))).thenReturn(sugestaoSalva);

      // When
      SugestaoDeProjetoResponseDTO result = service.criar(requestDTO);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(3L);
      assertThat(result.getTitulo()).isEqualTo("Projeto de IA");
      assertThat(result.getDescricao()).isEqualTo("Sistema de inteligência artificial para análise de dados");
      assertThat(result.getPublicoAlvo()).isEqualTo("Desenvolvedores e cientistas de dados");
      assertThat(result.getAlunoId()).isEqualTo(1L);
      assertThat(result.getAlunoNome()).isEqualTo("João Silva");
      assertThat(result.getProfessorId()).isEqualTo(2L);
      assertThat(result.getProfessorNome()).isEqualTo("Dr. Maria Santos");
      assertThat(result.getStatus()).isEqualTo(StatusSugestao.AGUARDANDO);
      assertThat(result.getDataCriacao()).isEqualTo(agora);

      verify(usuarioService).validarProfessor(professor);
  }

  // --- TESTES PARA LISTAR TODOS ---
  @Test
  void listarTodos_ComDados_DeveRetornarListaDeResponseDTO() {
      // Given
      Usuario aluno1 = new Usuario();
      aluno1.setId(1L);
      aluno1.setNome("João Silva");

      Usuario aluno2 = new Usuario();
      aluno2.setId(2L);
      aluno2.setNome("Maria Santos");

      SugestaoDeProjeto sugestao1 = new SugestaoDeProjeto();
      sugestao1.setId(1L);
      sugestao1.setTitulo("Projeto 1");
      sugestao1.setDescricao("Descrição do projeto 1");
      sugestao1.setPublicoAlvo("Estudantes");
      sugestao1.setAluno(aluno1);
      sugestao1.setStatus(StatusSugestao.AGUARDANDO);
      sugestao1.setDataCriacao(LocalDateTime.now());

      SugestaoDeProjeto sugestao2 = new SugestaoDeProjeto();
      sugestao2.setId(2L);
      sugestao2.setTitulo("Projeto 2");
      sugestao2.setDescricao("Descrição do projeto 2");
      sugestao2.setPublicoAlvo("Professores");
      sugestao2.setAluno(aluno2);
      sugestao2.setStatus(StatusSugestao.APROVADA);
      sugestao2.setDataCriacao(LocalDateTime.now());

      when(repository.findAll()).thenReturn(Arrays.asList(sugestao1, sugestao2));

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarTodos();

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);

      SugestaoDeProjetoResponseDTO response1 = result.get(0);
      assertThat(response1.getId()).isEqualTo(1L);
      assertThat(response1.getTitulo()).isEqualTo("Projeto 1");
      assertThat(response1.getStatus()).isEqualTo(StatusSugestao.AGUARDANDO);

      SugestaoDeProjetoResponseDTO response2 = result.get(1);
      assertThat(response2.getId()).isEqualTo(2L);
      assertThat(response2.getTitulo()).isEqualTo("Projeto 2");
      assertThat(response2.getStatus()).isEqualTo(StatusSugestao.APROVADA);

      verify(repository).findAll();
  }

  @Test
  void listarTodos_SemDados_DeveRetornarListaVazia() {
      // Given
      when(repository.findAll()).thenReturn(Collections.emptyList());

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarTodos();

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(repository).findAll();
  }

  // --- TESTES PARA LISTAR POR ALUNO ---
  @Test
  void listarPorAluno_ComAlunoValido_DeveRetornarListaDeSugestoes() {
      // Given
      Long alunoId = 1L;
      Usuario aluno = new Usuario();
      aluno.setId(alunoId);
      aluno.setNome("João Silva");

      SugestaoDeProjeto sugestao1 = new SugestaoDeProjeto();
      sugestao1.setId(1L);
      sugestao1.setTitulo("Projeto Mobile");
      sugestao1.setDescricao("Aplicativo mobile para estudantes");
      sugestao1.setPublicoAlvo("Estudantes universitários");
      sugestao1.setAluno(aluno);
      sugestao1.setStatus(StatusSugestao.AGUARDANDO);

      SugestaoDeProjeto sugestao2 = new SugestaoDeProjeto();
      sugestao2.setId(2L);
      sugestao2.setTitulo("Projeto Web");
      sugestao2.setDescricao("Sistema web para gestão acadêmica");
      sugestao2.setPublicoAlvo("Administradores");
      sugestao2.setAluno(aluno);
      sugestao2.setStatus(StatusSugestao.APROVADA);

      when(repository.findByAlunoId(alunoId)).thenReturn(Arrays.asList(sugestao1, sugestao2));

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarPorAluno(alunoId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      assertThat(result.get(0).getTitulo()).isEqualTo("Projeto Mobile");
      assertThat(result.get(0).getAlunoId()).isEqualTo(alunoId);
      assertThat(result.get(1).getId()).isEqualTo(2L);
      assertThat(result.get(1).getTitulo()).isEqualTo("Projeto Web");
      assertThat(result.get(1).getAlunoId()).isEqualTo(alunoId);

      verify(repository).findByAlunoId(alunoId);
  }

  @Test
  void listarPorAluno_AlunoSemSugestoes_DeveRetornarListaVazia() {
      // Given
      Long alunoId = 1L;
      when(repository.findByAlunoId(alunoId)).thenReturn(Collections.emptyList());

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarPorAluno(alunoId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(repository).findByAlunoId(alunoId);
  }

  // --- TESTES PARA LISTAR SUGESTÕES DO USUÁRIO LOGADO ---
  @Test
  void listarSugestoesDoUsuarioLogado_ComSugestoes_DeveRetornarListaDoUsuario() {
      // Given
      Usuario usuarioLogado = new Usuario();
      usuarioLogado.setId(5L);
      usuarioLogado.setNome("Carlos Oliveira");

      SugestaoDeProjeto sugestao1 = new SugestaoDeProjeto();
      sugestao1.setId(10L);
      sugestao1.setTitulo("Minha Sugestão 1");
      sugestao1.setDescricao("Primeira sugestão do usuário logado");
      sugestao1.setPublicoAlvo("Comunidade acadêmica");
      sugestao1.setAluno(usuarioLogado);
      sugestao1.setStatus(StatusSugestao.AGUARDANDO);

      SugestaoDeProjeto sugestao2 = new SugestaoDeProjeto();
      sugestao2.setId(11L);
      sugestao2.setTitulo("Minha Sugestão 2");
      sugestao2.setDescricao("Segunda sugestão do usuário logado");
      sugestao2.setPublicoAlvo("Estudantes de TI");
      sugestao2.setAluno(usuarioLogado);
      sugestao2.setStatus(StatusSugestao.REJEITADA);

      when(usuarioService.obterUsuarioLogado()).thenReturn(usuarioLogado);
      when(repository.findByAlunoId(5L)).thenReturn(Arrays.asList(sugestao1, sugestao2));

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarSugestoesDoUsuarioLogado();

      // Then
      assertThat(result).isNotNull();
      assertThat(result).hasSize(2);
      assertThat(result.get(0).getId()).isEqualTo(10L);
      assertThat(result.get(0).getTitulo()).isEqualTo("Minha Sugestão 1");
      assertThat(result.get(0).getAlunoId()).isEqualTo(5L);
      assertThat(result.get(0).getStatus()).isEqualTo(StatusSugestao.AGUARDANDO);
      assertThat(result.get(1).getId()).isEqualTo(11L);
      assertThat(result.get(1).getTitulo()).isEqualTo("Minha Sugestão 2");
      assertThat(result.get(1).getAlunoId()).isEqualTo(5L);
      assertThat(result.get(1).getStatus()).isEqualTo(StatusSugestao.REJEITADA);

      verify(usuarioService).obterUsuarioLogado();
      verify(repository).findByAlunoId(5L);
  }

  @Test
  void listarSugestoesDoUsuarioLogado_UsuarioSemSugestoes_DeveRetornarListaVazia() {
      // Given
      Usuario usuarioLogado = new Usuario();
      usuarioLogado.setId(3L);
      usuarioLogado.setNome("Ana Costa");

      when(usuarioService.obterUsuarioLogado()).thenReturn(usuarioLogado);
      when(repository.findByAlunoId(3L)).thenReturn(Collections.emptyList());

      // When
      List<SugestaoDeProjetoResponseDTO> result = service.listarSugestoesDoUsuarioLogado();

      // Then
      assertThat(result).isNotNull();
      assertThat(result).isEmpty();

      verify(usuarioService).obterUsuarioLogado();
      verify(repository).findByAlunoId(3L);
  }

  // --- TESTES PARA BUSCAR POR ID ---
  @Test
  void buscarPorId_ComIdExistente_DeveRetornarSugestao() {
      // Given
      Long sugestaoId = 15L;
      Usuario aluno = new Usuario();
      aluno.setId(1L);
      aluno.setNome("Pedro Santos");

      Usuario professor = new Usuario();
      professor.setId(2L);
      professor.setNome("Prof. Silva");

      LocalDateTime dataCriacao = LocalDateTime.of(2024, 1, 15, 10, 30);

      SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
      sugestao.setId(sugestaoId);
      sugestao.setTitulo("Projeto de Pesquisa");
      sugestao.setDescricao("Descrição detalhada do projeto de pesquisa científica");
      sugestao.setPublicoAlvo("Pesquisadores e estudantes de pós-graduação");
      sugestao.setAluno(aluno);
      sugestao.setProfessor(professor);
      sugestao.setStatus(StatusSugestao.APROVADA);
      sugestao.setDataCriacao(dataCriacao);

      when(repository.findById(sugestaoId)).thenReturn(Optional.of(sugestao));

      // When
      SugestaoDeProjetoResponseDTO result = service.buscarPorId(sugestaoId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(sugestaoId);
      assertThat(result.getTitulo()).isEqualTo("Projeto de Pesquisa");
      assertThat(result.getDescricao()).isEqualTo("Descrição detalhada do projeto de pesquisa científica");
      assertThat(result.getPublicoAlvo()).isEqualTo("Pesquisadores e estudantes de pós-graduação");
      assertThat(result.getAlunoId()).isEqualTo(1L);
      assertThat(result.getAlunoNome()).isEqualTo("Pedro Santos");
      assertThat(result.getProfessorId()).isEqualTo(2L);
      assertThat(result.getProfessorNome()).isEqualTo("Prof. Silva");
      assertThat(result.getStatus()).isEqualTo(StatusSugestao.APROVADA);
      assertThat(result.getDataCriacao()).isEqualTo(dataCriacao);

      verify(repository).findById(sugestaoId);
  }

  @Test
  void buscarPorId_ComIdInexistente_DeveLancarEntityNotFoundException() {
      // Given
      Long sugestaoIdInexistente = 999L;
      when(repository.findById(sugestaoIdInexistente)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> service.buscarPorId(sugestaoIdInexistente))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessage("Sugestão de projeto não encontrada");

      verify(repository).findById(sugestaoIdInexistente);
  }
}
