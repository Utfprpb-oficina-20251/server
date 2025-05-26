package br.edu.utfpr.pb.ext.server.projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Date;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;  

@ExtendWith(MockitoExtension.class)
class ProjetoControllerTest {

  @InjectMocks private ProjetoController projetoController;

  @Mock private IProjetoService projetoService;

  @Mock private UsuarioRepository usuarioRepository;

  @Mock private ModelMapper modelMapper;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(projetoController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldCreateProjetoSuccessfully() {
    // Arrange
    ProjetoDTO projetoDTO = new ProjetoDTO();
    projetoDTO.setTitulo("Projeto Teste");
    new UsuarioProjetoDTO();
    projetoDTO.setEquipeExecutora(
        List.of(
            UsuarioProjetoDTO.builder()
                .nomeCompleto("batata")
                .emailInstitucional("batata@utfpr.edu.br")
                .build()));

    Usuario usuario = new Usuario();
    usuario.setEmail("batata@utfpr.edu.br");

    Projeto projeto = new Projeto();
    projeto.setTitulo("Projeto Teste");

    when(usuarioRepository.findByEmail("batata@utfpr.edu.br")).thenReturn(Optional.of(usuario));
    when(projetoService.save(any(Projeto.class))).thenReturn(projeto);
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDTO);

    // Act
    ResponseEntity<ProjetoDTO> response = projetoController.create(projetoDTO);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Projeto Teste", response.getBody().getTitulo());
  }

@Test
  void testFindAll() throws Exception {
    Projeto projeto = new Projeto();
    projeto.setId(1L);
    projeto.setTitulo("Projeto Teste");

    ProjetoDTO dto = new ProjetoDTO();
    dto.setId(1L);
    dto.setTitulo("Projeto Teste");

    when(projetoService.findAll()).thenReturn(List.of(projeto));
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(dto);

    mockMvc.perform(get("/projeto").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].titulo").value("Projeto Teste"));
  }

  @Test
  void testUpdateSuccess() throws Exception {
    Long projetoId = 77L;

    LocalDate localDateInicio = LocalDate.parse("2025-06-01");
    Date dataInicio = Date.from(localDateInicio.atStartOfDay(ZoneId.systemDefault()).toInstant());

    LocalDate localDateFim = LocalDate.parse("2025-06-30");
    Date dataFim = Date.from(localDateFim.atStartOfDay(ZoneId.systemDefault()).toInstant());
    
    UsuarioProjetoDTO usuarioDto = new UsuarioProjetoDTO();
    usuarioDto.setId(30L);
    usuarioDto.setNomeCompleto("João da Silva");
    usuarioDto.setEmailInstitucional("joao@utfpr.edu.br");

    ProjetoDTO projetoDto = new ProjetoDTO();
    projetoDto.setId(projetoId);
    projetoDto.setTitulo("Feira de Ciências Atualizada");
    projetoDto.setDescricao("Uma feira com apresentações de projetos atualizada.");
    projetoDto.setJustificativa("Divulgar conhecimento científico ampliado.");
    projetoDto.setDataInicio(dataInicio);
    projetoDto.setDataFim(dataFim);
    projetoDto.setPublicoAlvo("Estudantes do ensino médio");
    projetoDto.setVinculadoDisciplina(true);
    projetoDto.setRestricaoPublico("Nenhuma");
    projetoDto.setStatus(StatusProjeto.EM_ANDAMENTO);
    projetoDto.setEquipeExecutora(List.of(usuarioDto));

    // Simula a busca do projeto existente
    Projeto projetoExistente = new Projeto();
    projetoExistente.setId(projetoId);
    when(projetoService.findById(projetoId)).thenReturn(Optional.of(projetoExistente));

    // Simula a busca do usuário pelo email
    Usuario usuario = new Usuario();
    usuario.setId(30L);
    usuario.setEmail("joao@utfpr.edu.br");
    when(usuarioRepository.findByEmail("joao@utfpr.edu.br")).thenReturn(Optional.of(usuario));

    // Simula o salvamento do projeto atualizado
    when(projetoService.save(any(Projeto.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // Simula o mapeamento do Projeto para ProjetoDTO
    when(modelMapper.map(any(Projeto.class), eq(ProjetoDTO.class))).thenReturn(projetoDto);

    // Serializa o DTO para JSON
    String json = new ObjectMapper().writeValueAsString(projetoDto);

    mockMvc.perform(put("/projeto/{id}", projetoId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Feira de Ciências Atualizada"))
        .andExpect(jsonPath("$.equipeExecutora[0].emailInstitucional").value("joao@utfpr.edu.br"));
  }
}