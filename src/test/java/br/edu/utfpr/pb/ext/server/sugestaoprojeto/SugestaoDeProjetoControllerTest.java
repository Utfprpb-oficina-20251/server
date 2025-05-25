package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import br.edu.utfpr.pb.ext.server.sugestaoprojeto.controller.SugestaoDeProjetoController;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.*;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class SugestaoDeProjetoControllerTest {

  @Mock private SugestaoDeProjetoService service;

  @InjectMocks private SugestaoDeProjetoController controller;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  // --- TESTE 1: Criar sugestão (POST /sugestao) ---
  @Test
  void criarSugestao_ComDadosValidos_DeveRetornar201() throws Exception {
    // 1. Configuração
    SugestaoDeProjetoRequestDTO request =
        SugestaoDeProjetoRequestDTO.builder()
            .titulo("Projeto Teste")
            .descricao("Esta descrição tem mais do que 30 caracteres obrigatórios")
            .publicoAlvo("Alunos")
            .build();

    SugestaoDeProjetoResponseDTO response =
        SugestaoDeProjetoResponseDTO.builder().id(1L).titulo(request.getTitulo()).build();

    when(service.criar(any())).thenReturn(response);

    // 2. Execução e Verificação
    mockMvc
        .perform(
            post("/sugestao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.titulo").value("Projeto Teste"));
  }

  // --- TESTE 2: Listar sugestões do usuário logado (GET /sugestao/minhas-sugestoes) ---
  @Test
  @WithMockUser
  void listarSugestoesDoUsuarioLogado_DeveRetornar200() throws Exception {
    // 1. Configuração
    SugestaoDeProjetoResponseDTO response =
        SugestaoDeProjetoResponseDTO.builder().id(1L).titulo("Projeto 1").build();

    when(service.listarSugestoesDoUsuarioLogado()).thenReturn(List.of(response));

    // 2. Execução e Verificação
    mockMvc
        .perform(get("/sugestao/minhas-sugestoes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1L))
        .andExpect(jsonPath("$[0].titulo").value("Projeto 1"));
  }
}
