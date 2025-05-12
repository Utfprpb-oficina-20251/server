package br.edu.utfpr.pb.ext.server;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ServerApplicationTests {
  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {
    // Só pra ver se carrega o contexto
  }

  @Test
  @Description("Consultar OPTIONS deve retornar cabeçalhos CORS")
  void options_whenCorsRequest_ShouldReturnCORSHeaders() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.options("/api/auth/login")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Access-Control-Allow-Origin"))
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
        .andExpect(header().exists("Access-Control-Allow-Methods"));
  }
}
