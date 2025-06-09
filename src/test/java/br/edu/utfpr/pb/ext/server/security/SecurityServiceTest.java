package br.edu.utfpr.pb.ext.server.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.projeto.Projeto;
import br.edu.utfpr.pb.ext.server.projeto.ProjetoRepository;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @InjectMocks
    private SecurityService securityService;

    @Mock
    private ProjetoRepository projetoRepository;

    // Mocks para simular o contexto de segurança
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        // Antes de cada teste, configuramos o SecurityContextHolder para usar nossos mocks.
        // Isso simula um usuário logado no sistema.
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // Limpa o contexto de segurança após cada teste para evitar que um teste interfira no outro.
        SecurityContextHolder.clearContext();
    }

    /**
     * Testa se  retorna TRUE quando o usuário logado FAZ PARTE da equipe executora.
     */
    @Test
    void podeEditarProjeto_quandoUsuarioEstaNaEquipe_deveRetornarTrue() {
        // Arrange (Organização)
        // 1. Cria os usuários e o projeto
        Usuario usuarioLogado = new Usuario();
        usuarioLogado.setId(10L);

        Usuario outroMembro = new Usuario();
        outroMembro.setId(20L);

        Projeto projeto = new Projeto();
        projeto.setId(1L);
        projeto.setEquipeExecutora(List.of(usuarioLogado, outroMembro));

        // 2. Simula o usuário logado no SecurityContextHolder
        when(authentication.getPrincipal()).thenReturn(usuarioLogado);

        // 3. Simula a busca do projeto no repositório
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        // Act (Ação)
        boolean temPermissao = securityService.podeEditarProjeto(1L);

        // Assert (Verificação)
        assertTrue(temPermissao);
    }

    /**
     * Testa se retorna FALSE quando o usuário logado NÃO FAZ PARTE da equipe executora.
     */
    @Test
    void podeEditarProjeto_quandoUsuarioNaoEstaNaEquipe_deveRetornarFalse() {
        // Arrange
        // 1. Cria os usuários e o projeto
        Usuario usuarioNaoAutorizado = new Usuario();
        usuarioNaoAutorizado.setId(99L); // ID que não está na equipe

        Usuario membroDaEquipe1 = new Usuario();
        membroDaEquipe1.setId(10L);
        Usuario membroDaEquipe2 = new Usuario();
        membroDaEquipe2.setId(20L);

        Projeto projeto = new Projeto();
        projeto.setId(1L);
        projeto.setEquipeExecutora(List.of(membroDaEquipe1, membroDaEquipe2));

        // 2. Simula o usuário NÃO AUTORIZADO como logado
        when(authentication.getPrincipal()).thenReturn(usuarioNaoAutorizado);

        // 3. Simula a busca do projeto no repositório
        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));

        // Act
        boolean temPermissao = securityService.podeEditarProjeto(1L);

        // Assert
        assertFalse(temPermissao);
    }

    /**
     * Testa se lança uma exceção quando o projeto a ser verificado não é encontrado.
     */
    @Test
    void podeEditarProjeto_quandoProjetoNaoExiste_deveLancarExcecao() {
        // Arrange
        Usuario usuarioQualquer = new Usuario();
        usuarioQualquer.setId(1L);

        // Simula o usuário logado
        when(authentication.getPrincipal()).thenReturn(usuarioQualquer);

        // Simula o repositório NÃO encontrando o projeto
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        // Verifica se a chamada  lança a exceção esperada
        assertThrows(RuntimeException.class, () -> securityService.podeEditarProjeto(99L));
    }
  /**
   * Testa o cenário de borda onde o projeto existe, mas sua equipe executora está vazia.
   * O resultado esperado é 'false', pois o usuário não pode pertencer a uma equipe vazia.
   */
  @Test
  void podeEditarProjeto_quandoEquipeExecutoraEstaVazia_deveRetornarFalse() {
    // Arrange
    // 1. Cria um usuário e um projeto com equipe vazia
    Usuario usuarioLogado = new Usuario();
    usuarioLogado.setId(10L);

    Projeto projetoComEquipeVazia = new Projeto();
    projetoComEquipeVazia.setId(1L);
    projetoComEquipeVazia.setEquipeExecutora(Collections.emptyList()); // Equipe vazia

    // 2. Simula o usuário logado
    when(authentication.getPrincipal()).thenReturn(usuarioLogado);

    // 3. Simula a busca do projeto
    when(projetoRepository.findById(1L)).thenReturn(Optional.of(projetoComEquipeVazia));

    // Act
    boolean temPermissao = securityService.podeEditarProjeto(1L);

    // Assert
    assertFalse(temPermissao);
  }
}