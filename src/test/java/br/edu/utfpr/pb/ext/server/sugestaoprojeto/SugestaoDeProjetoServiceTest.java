package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.*;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SugestaoDeProjetoServiceTest {

  @Mock private SugestaoDeProjetoRepository repository;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private UsuarioService usuarioService;

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
    SugestaoDeProjetoResponseDTO response = service.criar(request);

    // 4. Verificações
    assertNull(sugestaoSalva.getProfessor(), "Professor deve ser null quando não informado");
    assertEquals("Projeto sem professor", response.getTitulo());
  }
}
