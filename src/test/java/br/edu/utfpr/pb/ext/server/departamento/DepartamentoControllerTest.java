package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada Controller.
 * Valida os endpoints de listagem e busca de departamento.
 */
@WebMvcTest(controllers = DepartamentoController.class)
public class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartamentoService service;

    @Test
    void deveListarTodosOsDepartamentos() throws Exception {
        DepartamentoDTO dto = new DepartamentoDTO();
        dto.setId(1L);
        dto.setDepartamento(Departamentos.DAQUI);
        dto.setResponsavelId(100L);

        when(service.findAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/departamentos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].departamento").value("DAQUI"))
                .andExpect(jsonPath("$[0].responsavelId").value(100L));
    }

    @Test
    void deveBuscarDepartamentoPorNome() throws Exception {
        DepartamentoDTO dto = new DepartamentoDTO();
        dto.setId(2L);
        dto.setDepartamento(Departamentos.DALET);
        dto.setResponsavelId(101L);

        when(service.findByDepartamento("DALET")).thenReturn(dto);

        mockMvc.perform(get("/api/departamentos/DALET")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departamento").value("DALET"))
                .andExpect(jsonPath("$.responsavelId").value(101L));
    }
}
