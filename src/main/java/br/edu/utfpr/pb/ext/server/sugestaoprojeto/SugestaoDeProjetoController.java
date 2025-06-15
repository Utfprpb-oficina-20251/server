package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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
   * Inicializa o controlador REST para sugestões de projeto com o serviço e o model mapper
   * fornecidos.
   *
   * @param service implementação do serviço responsável pelas operações de sugestão de projeto
   * @param modelMapper instância utilizada para conversão entre entidades e DTOs
   */
  public SugestaoDeProjetoController(
      SugestaoDeProjetoServiceImpl service, ModelMapper modelMapper) {
    super(SugestaoDeProjeto.class, SugestaoDeProjetoDTO.class);
    this.service = service;
    this.modelMapper = modelMapper;
  }

  /**
   * Fornece o serviço utilizado para operações CRUD de sugestões de projeto.
   *
   * @return instância do serviço de SugestaoDeProjeto
   */
  @Override
  protected ICrudService<SugestaoDeProjeto, Long> getService() {
    return this.service;
  }

  /**
   * Fornece o ModelMapper configurado para conversão entre entidades e DTOs neste controlador.
   *
   * @return o ModelMapper utilizado para mapeamento de entidades e DTOs
   */
  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

  /**
   * Retorna uma lista das sugestões de projeto vinculadas ao usuário atualmente autenticado.
   *
   * @return resposta HTTP 200 contendo a lista de SugestaoDeProjetoDTO referentes ao usuário logado
   */
  @Operation(summary = "Listar sugestões de projeto do usuário logado")
  @GetMapping("/minhas-sugestoes")
  public ResponseEntity<List<SugestaoDeProjetoDTO>> listarSugestoesDoUsuarioLogado() {
    List<SugestaoDeProjeto> sugestoes = service.listarSugestoesDoUsuarioLogado();
    return ResponseEntity.ok(sugestoes.stream().map(this::convertToResponseDTO).toList());
  }

  /**
   * Converte uma entidade SugestaoDeProjeto para seu DTO correspondente.
   *
   * @param sugestaoDeProjeto entidade de sugestão de projeto a ser convertida
   * @return DTO da sugestão de projeto
   */
  private SugestaoDeProjetoDTO convertToResponseDTO(SugestaoDeProjeto sugestaoDeProjeto) {
    return getModelMapper().map(sugestaoDeProjeto, SugestaoDeProjetoDTO.class);
  }

  /**
   * Cria uma nova sugestão de projeto e retorna mensagem de sucesso para toast.
   *
   * @param sugestaoDTO dados da sugestão a ser criada
   * @return resposta padronizada com mensagem de sucesso
   */
  @Override
  @PostMapping
  @Operation(summary = "Cria uma nova sugestão de projeto")
  public ResponseEntity<SugestaoDeProjetoDTO> create(
      @RequestBody @Valid SugestaoDeProjetoDTO sugestaoDTO) {
    SugestaoDeProjeto sugestaoSalva = getService().save(convertToEntity(sugestaoDTO));
    SugestaoDeProjetoDTO sugestaoSalvaDTO =
        getModelMapper().map(sugestaoSalva, SugestaoDeProjetoDTO.class);
    return ResponseEntity.status(HttpStatus.CREATED).body(sugestaoSalvaDTO);
  }
}
