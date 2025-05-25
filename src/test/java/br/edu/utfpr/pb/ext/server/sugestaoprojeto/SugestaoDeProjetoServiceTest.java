package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.*;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoService;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class SugestaoDeProjetoServiceTest {

  @Mock private SugestaoDeProjetoRepository repository;

  @Mock private UsuarioRepository usuarioRepository;

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

    // Mock do usuário autenticado
    Authentication auth = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    when(auth.getPrincipal()).thenReturn(aluno);
    SecurityContextHolder.setContext(securityContext);

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
}
