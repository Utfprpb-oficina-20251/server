package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestao")
@Tag(
    name = "SugestaoDeProjetoController",
    description = "Endpoints para gerenciar sugestões de projeto")
public class SugestaoDeProjetoController
    extends CrudController<SugestaoDeProjeto, SugestaoDeProjetoDTO, Long> {

  private final SugestaoDeProjetoServiceImpl service;
  private final ModelMapper modelMapper;

  /**
   * Cria uma instância do controlador de sugestões de projeto, inicializando o serviço e o model
   * mapper necessários.
   *
   * @param service implementação do serviço de sugestões de projeto
   * @param modelMapper instância para conversão entre entidades e DTOs
   */
  public SugestaoDeProjetoController(
      SugestaoDeProjetoServiceImpl service, ModelMapper modelMapper) {
    super(SugestaoDeProjeto.class, SugestaoDeProjetoDTO.class);
    this.service = service;
    this.modelMapper = modelMapper;
  }

  /**
   * Retorna a instância do serviço responsável pelas operações de CRUD para SugestaoDeProjeto.
   *
   * @return o serviço de SugestaoDeProjeto utilizado pelo controlador
   */
  @Override
  protected ICrudService<SugestaoDeProjeto, Long> getService() {
    return this.service;
  }

  /**
   * Retorna a instância de ModelMapper utilizada para conversão entre entidades e DTOs.
   *
   * @return o ModelMapper configurado para este controlador
   */
  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

  /**
   * Lista as sugestões de projeto do usuário atualmente autenticado.
   *
   * @return uma resposta HTTP 200 contendo a lista de sugestões de projeto do usuário logado, no
   *     formato DTO
   */
  @Operation(summary = "Listar sugestões de projeto do usuário logado")
  @GetMapping("/minhas-sugestoes")
  public ResponseEntity<List<SugestaoDeProjetoDTO>> listarSugestoesDoUsuarioLogado() {
    List<SugestaoDeProjeto> sugestoes = service.listarSugestoesDoUsuarioLogado();
    return ResponseEntity.ok(sugestoes.stream().map(this::convertToResponseDTO).toList());
  }

  /**
   * Converte uma entidade SugestaoDeProjeto em seu DTO correspondente.
   *
   * @param sugestaoDeProjeto entidade de sugestão de projeto a ser convertida
   * @return DTO representando a sugestão de projeto fornecida
   */
  private SugestaoDeProjetoDTO convertToResponseDTO(SugestaoDeProjeto sugestaoDeProjeto) {
    return getModelMapper().map(sugestaoDeProjeto, SugestaoDeProjetoDTO.class);
  }
}
