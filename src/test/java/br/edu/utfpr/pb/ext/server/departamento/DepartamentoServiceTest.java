package br.edu.utfpr.pb.ext.server.departamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para a classe DepartamentoServiceImpl. Verifica o funcionamento dos métodos de
 * negócio da camada de serviço.
 */
public class DepartamentoServiceTest {

  private DepartamentoRepository departamentoRepository;
  private UsuarioRepository usuarioRepository;
  private DepartamentoServiceImpl service;

  /** Inicializa os mocks dos repositórios e a instância do serviço antes de cada teste. */
  @BeforeEach
  void setUp() {
    departamentoRepository = mock(DepartamentoRepository.class);
    usuarioRepository = mock(UsuarioRepository.class);
    service = new DepartamentoServiceImpl(departamentoRepository, usuarioRepository);
  }

  /** Testa se o método findAll() retorna corretamente todos os departamentos simulados. */
  @Test
  void shouldFindAllDepartamentos() {
    List<Departamento> mockList =
        Arrays.asList(
            createDepartamento(1L, "DAINF", "Departamento de Computação"),
            createDepartamento(2L, "DAMAT", "Departamento de Matemática"));

    when(departamentoRepository.findAll()).thenReturn(mockList);

    List<Departamento> result = service.findAll();

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getSigla()).isEqualTo("DAINF");
  }

  /** Testa se o método findOne() retorna corretamente um departamento pelo ID. */
  @Test
  void shouldFindById() {
    Departamento departamento = createDepartamento(1L, "DAINF", "Computação");
    when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));

    Departamento result = service.findOne(1L);

    assertThat(result).isNotNull();
    assertThat(result.getSigla()).isEqualTo("DAINF");
  }

  /** Testa se o método save() persiste um novo departamento e retorna a entidade salva. */
  @Test
  void shouldSaveDepartamento() {
    Departamento departamento = createDepartamento(null, "DAINF", "Computação");
    Departamento saved = createDepartamento(1L, "DAINF", "Computação");

    when(departamentoRepository.save(departamento)).thenReturn(saved);

    Departamento result = service.save(departamento);

    assertThat(result.getId()).isEqualTo(1L);
  }

  /** Testa se o método delete() chama corretamente a exclusão do repositório por ID. */
  @Test
  void shouldDeleteById() {
    doNothing().when(departamentoRepository).deleteById(1L);
    service.delete(1L);
    verify(departamentoRepository, times(1)).deleteById(1L);
  }

  /** Testa se o método associarResponsavel() associa corretamente um usuário ao departamento. */
  @Test
  void shouldAssociateResponsavelToDepartamento() {
    Departamento departamento = createDepartamento(1L, "DAINF", "Computação");
    Usuario usuario = new Usuario();
    usuario.setId(10L);

    when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
    when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

    service.associarResponsavel(1L, 10L);

    assertThat(departamento.getResponsavel()).isEqualTo(usuario);
    verify(departamentoRepository).save(departamento);
  }

  /** Testa se exceção é lançada quando departamento não for encontrado na associação. */
  @Test
  void shouldThrowExceptionWhenDepartamentoNotFoundInAssociarResponsavel() {
    when(departamentoRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.associarResponsavel(999L, 10L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Departamento não encontrado com ID: 999");
    }

    /** Testa se exceção é lançada quando usuário não for encontrado na associação. */
    @Test
    void shouldThrowExceptionWhenUsuarioNotFoundInAssociarResponsavel() {
    Departamento departamento = createDepartamento(1L, "DAINF", "Computação");
    when(departamentoRepository.findById(1L)).thenReturn(Optional.of(departamento));
    when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.associarResponsavel(1L, 999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Usuário não encontrado com ID: 999");
  }

  /** Testa se exceção é lançada ao buscar departamento inexistente por ID. */
  @Test
  void shouldThrowExceptionWhenDepartamentoNotFound() {
    when(departamentoRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findOne(999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Departamento não encontrado com ID: 999");
  }

  /** Método auxiliar para criar instâncias de Departamento durante os testes. */
  private Departamento createDepartamento(Long id, String sigla, String nome) {
    Departamento d = new Departamento();
    d.setId(id);
    d.setSigla(sigla);
    d.setNome(nome);
    return d;
  }
}
