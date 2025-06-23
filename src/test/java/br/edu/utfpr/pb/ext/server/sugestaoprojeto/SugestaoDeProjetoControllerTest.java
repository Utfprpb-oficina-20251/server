package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.utfpr.pb.ext.server.curso.Curso;
import br.edu.utfpr.pb.ext.server.curso.dto.CursoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioNomeIdDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SugestaoDeProjetoControllerTest {

  @Mock private SugestaoDeProjetoServiceImpl service;

  @Spy private ModelMapper modelMapper;

  @InjectMocks private SugestaoDeProjetoController controller;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules(); // For handling LocalDateTime
  }

  @Test
  void findAll_shouldReturnAllSugestoes() throws Exception {
    // Arrange
    Curso curso1 = createCurso(1L, "Ciência da Computação", "DAINF");
    Curso curso2 = createCurso(2L, "Engenharia da Computação", "DAEGC");
    CursoDTO curso1DTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    CursoDTO curso2DTO = createCursoDTO(2L, "Engenharia da Computação", "DAEGC");

    SugestaoDeProjeto sugestao1 =
        createSugestaoDeProjeto(1L, "Título 1", "Descrição 1", "Público Alvo 1", curso1);
    SugestaoDeProjeto sugestao2 =
        createSugestaoDeProjeto(2L, "Título 2", "Descrição 2", "Público Alvo 2", curso2);
    List<SugestaoDeProjeto> sugestoes = Arrays.asList(sugestao1, sugestao2);

    SugestaoDeProjetoDTO dto1 =
        createSugestaoDeProjetoDTO(1L, "Título 1", "Descrição 1", "Público Alvo 1", curso1DTO);
    SugestaoDeProjetoDTO dto2 =
        createSugestaoDeProjetoDTO(2L, "Título 2", "Descrição 2", "Público Alvo 2", curso2DTO);

    when(service.findAll()).thenReturn(sugestoes);
    when(modelMapper.map(sugestao1, SugestaoDeProjetoDTO.class)).thenReturn(dto1);
    when(modelMapper.map(sugestao2, SugestaoDeProjetoDTO.class)).thenReturn(dto2);

    // Act & Assert
    mockMvc
        .perform(get("/sugestao"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].titulo", is("Título 1")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].titulo", is("Título 2")));

    verify(service).findAll();
  }

  @Test
  void findOne_whenExists_shouldReturnSugestao() throws Exception {
    // Arrange
    Long id = 1L;
    Curso curso = createCurso(id, "Ciência da Computação", "DAINF");
    CursoDTO cursoDTO = createCursoDTO(id, "Ciência da Computação", "DAINF");
    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(id, "Título 1", "Descrição 1", "Público Alvo 1", curso);
    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(id, "Título 1", "Descrição 1", "Público Alvo 1", cursoDTO);

    when(service.findOne(id)).thenReturn(sugestao);
    when(modelMapper.map(sugestao, SugestaoDeProjetoDTO.class)).thenReturn(dto);

    // Act & Assert
    mockMvc
        .perform(get("/sugestao/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Título 1")))
        .andExpect(jsonPath("$.descricao", is("Descrição 1")))
        .andExpect(jsonPath("$.publicoAlvo", is("Público Alvo 1")))
        .andExpect(jsonPath("$.curso.id", is(1)))
        .andExpect(jsonPath("$.curso.nome", is("Ciência da Computação")))
        .andExpect(jsonPath("$.curso.codigo", is("DAINF")));

    verify(service).findOne(id);
  }

  @Test
  void findOne_whenNotExists_shouldReturnNotFound() throws Exception {
    // Arrange
    Long id = 1L;
    when(service.findOne(id)).thenThrow(EntityNotFoundException.class);

    // Act & Assert
    mockMvc.perform(get("/sugestao/{id}", id)).andExpect(status().isNotFound());

    verify(service).findOne(id);
  }

  @Test
  void create_comImagemUrlBase64Valida_deveCriarSugestaoComImagemProcessada() throws Exception {
    // Arrange
    String base64Image =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wcAAwAB/epv2AAAAABJRU5ErkJggg==";
    String finalUrl = "http://storage/sugestao-imagem.png";
    String descricao =
        "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    CursoDTO cursoDTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    Curso curso = createCurso(1L, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(null, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    dto.setImagemUrl(base64Image);

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(null, "Novo Título", descricao, "Novo Público Alvo", curso);
    sugestao.setImagemUrl(base64Image);

    SugestaoDeProjeto savedSugestao =
        createSugestaoDeProjeto(1L, "Novo Título", descricao, "Novo Público Alvo", curso);
    savedSugestao.setImagemUrl(finalUrl);

    SugestaoDeProjetoDTO savedDto =
        createSugestaoDeProjetoDTO(1L, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    savedDto.setImagemUrl(finalUrl);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(savedSugestao);
    when(modelMapper.map(savedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(savedDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Novo Título")))
        .andExpect(jsonPath("$.imagemUrl", is(finalUrl)));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void create_comImagemUrlHttpValida_deveCriarSugestaoSemProcessamento() throws Exception {
    // Arrange
    String httpImageUrl = "https://example.com/image.jpg";
    String descricao =
        "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    CursoDTO cursoDTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    Curso curso = createCurso(1L, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(null, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    dto.setImagemUrl(httpImageUrl);

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(null, "Novo Título", descricao, "Novo Público Alvo", curso);
    sugestao.setImagemUrl(httpImageUrl);

    SugestaoDeProjeto savedSugestao =
        createSugestaoDeProjeto(1L, "Novo Título", descricao, "Novo Público Alvo", curso);
    savedSugestao.setImagemUrl(httpImageUrl);

    SugestaoDeProjetoDTO savedDto =
        createSugestaoDeProjetoDTO(1L, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    savedDto.setImagemUrl(httpImageUrl);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(savedSugestao);
    when(modelMapper.map(savedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(savedDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Novo Título")))
        .andExpect(jsonPath("$.imagemUrl", is(httpImageUrl)));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void create_comImagemUrlNula_deveCriarSugestaoSemImagem() throws Exception {
    // Arrange
    String descricao =
        "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    CursoDTO cursoDTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    Curso curso = createCurso(1L, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(null, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    dto.setImagemUrl(null);

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(null, "Novo Título", descricao, "Novo Público Alvo", curso);
    sugestao.setImagemUrl(null);

    SugestaoDeProjeto savedSugestao =
        createSugestaoDeProjeto(1L, "Novo Título", descricao, "Novo Público Alvo", curso);
    savedSugestao.setImagemUrl(null);

    SugestaoDeProjetoDTO savedDto =
        createSugestaoDeProjetoDTO(1L, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    savedDto.setImagemUrl(null);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(savedSugestao);
    when(modelMapper.map(savedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(savedDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Novo Título")))
        .andExpect(jsonPath("$.imagemUrl").doesNotExist());

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void create_comImagemUrlVazia_deveCriarSugestaoSemImagem() throws Exception {
    // Arrange
    String descricao =
        "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    CursoDTO cursoDTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    Curso curso = createCurso(1L, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(null, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    dto.setImagemUrl("");

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(null, "Novo Título", descricao, "Novo Público Alvo", curso);
    sugestao.setImagemUrl("");

    SugestaoDeProjeto savedSugestao =
        createSugestaoDeProjeto(1L, "Novo Título", descricao, "Novo Público Alvo", curso);
    savedSugestao.setImagemUrl("");

    SugestaoDeProjetoDTO savedDto =
        createSugestaoDeProjetoDTO(1L, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    savedDto.setImagemUrl("");

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(savedSugestao);
    when(modelMapper.map(savedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(savedDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Novo Título")))
        .andExpect(jsonPath("$.imagemUrl", is("")));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void update_comNovaImagemUrl_deveAtualizarSugestaoComImagemProcessada() throws Exception {
    // Arrange
    Long id = 1L;
    String novaImagemBase64 = "data:image/jpeg;base64,validBase64String";
    String finalUrl = "http://storage/sugestao-imagem.jpg";
    String descricao =
        "Esta é uma descrição atualizada longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    Curso curso = createCurso(id, "Ciência da Computação", "DAINF");
    CursoDTO cursoDTO = createCursoDTO(id, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);
    dto.setImagemUrl(novaImagemBase64);

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    sugestao.setImagemUrl(novaImagemBase64);

    SugestaoDeProjeto updatedSugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    updatedSugestao.setImagemUrl(finalUrl);

    SugestaoDeProjetoDTO updatedDto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);
    updatedDto.setImagemUrl(finalUrl);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(updatedSugestao);
    when(modelMapper.map(updatedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(updatedDto);

    // Act & Assert
    mockMvc
        .perform(
            put("/sugestao/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Título Atualizado")))
        .andExpect(jsonPath("$.imagemUrl", is(finalUrl)));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void update_removendoImagemUrl_deveAtualizarSugestaoSemImagem() throws Exception {
    // Arrange
    Long id = 1L;
    String descricao =
        "Esta é uma descrição atualizada longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";

    Curso curso = createCurso(id, "Ciência da Computação", "DAINF");
    CursoDTO cursoDTO = createCursoDTO(id, "Ciência da Computação", "DAINF");

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);
    dto.setImagemUrl(null);

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    sugestao.setImagemUrl(null);

    SugestaoDeProjeto updatedSugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    updatedSugestao.setImagemUrl(null);

    SugestaoDeProjetoDTO updatedDto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);
    updatedDto.setImagemUrl(null);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(updatedSugestao);
    when(modelMapper.map(updatedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(updatedDto);

    // Act & Assert
    mockMvc
        .perform(
            put("/sugestao/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Título Atualizado")))
        .andExpect(jsonPath("$.imagemUrl").doesNotExist());

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void findOne_comImagemUrl_deveRetornarSugestaoComImagem() throws Exception {
    // Arrange
    Long id = 1L;
    String imagemUrl = "http://storage/sugestao-imagem.png";

    Curso curso = createCurso(id, "Ciência da Computação", "DAINF");
    CursoDTO cursoDTO = createCursoDTO(id, "Ciência da Computação", "DAINF");

    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(id, "Título 1", "Descrição 1", "Público Alvo 1", curso);
    sugestao.setImagemUrl(imagemUrl);

    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(id, "Título 1", "Descrição 1", "Público Alvo 1", cursoDTO);
    dto.setImagemUrl(imagemUrl);

    when(service.findOne(id)).thenReturn(sugestao);
    when(modelMapper.map(sugestao, SugestaoDeProjetoDTO.class)).thenReturn(dto);

    // Act & Assert
    mockMvc
        .perform(get("/sugestao/{id}", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Título 1")))
        .andExpect(jsonPath("$.imagemUrl", is(imagemUrl)));

    verify(service).findOne(id);
  }

  @Test
  void create_shouldCreateSugestao() throws Exception {
    // Arrange
    CursoDTO cursoDTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    Curso curso = createCurso(1L, "Ciência da Computação", "DAINF");

    String descricao =
        "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";
    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(null, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);
    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(null, "Novo Título", descricao, "Novo Público Alvo", curso);
    SugestaoDeProjeto savedSugestao =
        createSugestaoDeProjeto(1L, "Novo Título", descricao, "Novo Público Alvo", curso);
    SugestaoDeProjetoDTO savedDto =
        createSugestaoDeProjetoDTO(1L, "Novo Título", descricao, "Novo Público Alvo", cursoDTO);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(savedSugestao);
    when(modelMapper.map(savedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(savedDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Novo Título")))
        .andExpect(
            jsonPath(
                "$.descricao",
                is(
                    "Esta é uma descrição longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.")))
        .andExpect(jsonPath("$.publicoAlvo", is("Novo Público Alvo")))
        .andExpect(jsonPath("$.curso.id", is(1)))
        .andExpect(jsonPath("$.curso.nome", is("Ciência da Computação")))
        .andExpect(jsonPath("$.curso.codigo", is("DAINF")));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void update_shouldUpdateSugestao() throws Exception {
    // Arrange
    Long id = 1L;
    Curso curso = createCurso(id, "Ciência da Computação", "DAINF");
    CursoDTO cursoDTO = createCursoDTO(id, "Ciência da Computação", "DAINF");

    String descricao =
        "Esta é uma descrição atualizada longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.";
    SugestaoDeProjetoDTO dto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);
    SugestaoDeProjeto sugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    SugestaoDeProjeto updatedSugestao =
        createSugestaoDeProjeto(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", curso);
    SugestaoDeProjetoDTO updatedDto =
        createSugestaoDeProjetoDTO(
            id, "Título Atualizado", descricao, "Público Alvo Atualizado", cursoDTO);

    when(modelMapper.map(dto, SugestaoDeProjeto.class)).thenReturn(sugestao);
    when(service.save(sugestao)).thenReturn(updatedSugestao);
    when(modelMapper.map(updatedSugestao, SugestaoDeProjetoDTO.class)).thenReturn(updatedDto);

    // Act & Assert
    mockMvc
        .perform(
            put("/sugestao/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.titulo", is("Título Atualizado")))
        .andExpect(
            jsonPath(
                "$.descricao",
                is(
                    "Esta é uma descrição atualizada longa o suficiente para passar na validação de tamanho mínimo de 30 caracteres.")))
        .andExpect(jsonPath("$.publicoAlvo", is("Público Alvo Atualizado")))
        .andExpect(jsonPath("$.curso.id", is(1)))
        .andExpect(jsonPath("$.curso.nome", is("Ciência da Computação")))
        .andExpect(jsonPath("$.curso.codigo", is("DAINF")));

    verify(service).save(any(SugestaoDeProjeto.class));
  }

  @Test
  void delete_shouldDeleteSugestao() throws Exception {
    // Arrange
    Long id = 1L;
    doNothing().when(service).delete(id);

    // Act & Assert
    mockMvc.perform(delete("/sugestao/{id}", id)).andExpect(status().isNoContent());

    verify(service).delete(id);
  }

  @Test
  void listarSugestoesDoUsuarioLogado_shouldReturnSugestoesDoUsuarioLogado() throws Exception {
    // Arrange
    Curso curso1 = createCurso(1L, "Ciência da Computação", "DAINF");
    Curso curso2 = createCurso(2L, "Engenharia da Computação", "DAEGC");
    CursoDTO curso1DTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    CursoDTO curso2DTO = createCursoDTO(2L, "Engenharia da Computação", "DAEGC");

    SugestaoDeProjeto sugestao1 =
        createSugestaoDeProjeto(1L, "Título 1", "Descrição 1", "Público Alvo 1", curso1);
    SugestaoDeProjeto sugestao2 =
        createSugestaoDeProjeto(2L, "Título 2", "Descrição 2", "Público Alvo 2", curso2);
    List<SugestaoDeProjeto> sugestoes = Arrays.asList(sugestao1, sugestao2);

    SugestaoDeProjetoDTO dto1 =
        createSugestaoDeProjetoDTO(1L, "Título 1", "Descrição 1", "Público Alvo 1", curso1DTO);
    SugestaoDeProjetoDTO dto2 =
        createSugestaoDeProjetoDTO(2L, "Título 2", "Descrição 2", "Público Alvo 2", curso2DTO);

    when(service.listarSugestoesDoUsuarioLogado()).thenReturn(sugestoes);
    when(modelMapper.map(sugestao1, SugestaoDeProjetoDTO.class)).thenReturn(dto1);
    when(modelMapper.map(sugestao2, SugestaoDeProjetoDTO.class)).thenReturn(dto2);

    // Act & Assert
    mockMvc
        .perform(get("/sugestao/minhas-sugestoes"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].titulo", is("Título 1")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].titulo", is("Título 2")));

    verify(service).listarSugestoesDoUsuarioLogado();
  }

  @Test
  void listarIndicacoesDoUsuarioLogado_shouldReturnIndicacoesDoUsuarioLogado() throws Exception {
    // Arrange
    Curso curso1 = createCurso(1L, "Ciência da Computação", "DAINF");
    Curso curso2 = createCurso(2L, "Engenharia da Computação", "DAEGC");
    CursoDTO curso1DTO = createCursoDTO(1L, "Ciência da Computação", "DAINF");
    CursoDTO curso2DTO = createCursoDTO(2L, "Engenharia da Computação", "DAEGC");

    SugestaoDeProjeto sugestao1 =
        createSugestaoDeProjeto(1L, "Indicação 1", "Descrição 1", "Público Alvo 1", curso1);
    SugestaoDeProjeto sugestao2 =
        createSugestaoDeProjeto(2L, "Indicação 2", "Descrição 2", "Público Alvo 2", curso2);
    List<SugestaoDeProjeto> sugestoes = Arrays.asList(sugestao1, sugestao2);

    SugestaoDeProjetoDTO dto1 =
        createSugestaoDeProjetoDTO(1L, "Indicação 1", "Descrição 1", "Público Alvo 1", curso1DTO);
    SugestaoDeProjetoDTO dto2 =
        createSugestaoDeProjetoDTO(2L, "Indicação 2", "Descrição 2", "Público Alvo 2", curso2DTO);

    when(service.listarIndicacoesDoUsuarioLogado()).thenReturn(sugestoes);
    when(modelMapper.map(sugestao1, SugestaoDeProjetoDTO.class)).thenReturn(dto1);
    when(modelMapper.map(sugestao2, SugestaoDeProjetoDTO.class)).thenReturn(dto2);

    // Act & Assert
    mockMvc
        .perform(get("/sugestao/minhas-indicacoes"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].titulo", is("Indicação 1")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].titulo", is("Indicação 2")));

    verify(service).listarIndicacoesDoUsuarioLogado();
  }

  // Helper methods to create test objects
  private SugestaoDeProjeto createSugestaoDeProjeto(
      Long id, String titulo, String descricao, String publicoAlvo, Curso curso) {
    SugestaoDeProjeto sugestao = new SugestaoDeProjeto();
    sugestao.setId(id);
    sugestao.setTitulo(titulo);
    sugestao.setDescricao(descricao);
    sugestao.setPublicoAlvo(publicoAlvo);
    sugestao.setStatus(StatusSugestao.AGUARDANDO);
    sugestao.setDataCriacao(LocalDateTime.now());
    sugestao.setCurso(curso);

    Usuario aluno = new Usuario();
    aluno.setId(1L);
    aluno.setNome("Aluno Teste");
    sugestao.setAluno(aluno);

    return sugestao;
  }

  private SugestaoDeProjetoDTO createSugestaoDeProjetoDTO(
      Long id, String titulo, String descricao, String publicoAlvo, CursoDTO curso) {

    SugestaoDeProjetoDTO dto = new SugestaoDeProjetoDTO();
    dto.setId(id);
    dto.setTitulo(titulo);
    dto.setDescricao(descricao);
    dto.setPublicoAlvo(publicoAlvo);
    dto.setStatus(StatusSugestao.AGUARDANDO);
    dto.setDataCriacao(LocalDateTime.now());
    dto.setCurso(curso);

    UsuarioNomeIdDTO aluno = new UsuarioNomeIdDTO();
    aluno.setId(1L);
    aluno.setNome("Aluno Teste");
    dto.setAluno(aluno);

    return dto;
  }

  private Curso createCurso(Long id, String nome, String codigo) {

    Curso curso = new Curso();
    curso.setId(id);
    curso.setNome(nome);
    curso.setCodigo(codigo);
    return curso;
  }

  private CursoDTO createCursoDTO(Long id, String nome, String codigo) {

    return CursoDTO.builder().id(id).nome(nome).codigo(codigo).build();
  }
}
