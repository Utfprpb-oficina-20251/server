package br.edu.utfpr.pb.ext.server.generics;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public abstract class CrudController<T extends BaseEntity, D, I extends Serializable> {

  /**
   * Retorna o serviço CRUD responsável pelas operações de persistência da entidade.
   *
   * @return implementação de ICrudService correspondente à entidade e ao identificador genéricos
   */
  protected abstract ICrudService<T, I> getService();

  /**
   * Fornece a instância de ModelMapper usada para converter entre entidades e DTOs neste
   * controlador CRUD.
   *
   * @return a instância de ModelMapper utilizada para mapeamento de tipos.
   */
  protected abstract ModelMapper getModelMapper();

  private final Class<T> typeClass;
  private final Class<D> typeDtoClass;

  /**
   * Inicializa o controlador CRUD genérico com as classes da entidade e do DTO especificados.
   *
   * @param typeClass classe da entidade a ser gerenciada
   * @param typeDtoClass classe do DTO correspondente à entidade
   */
  protected CrudController(Class<T> typeClass, Class<D> typeDtoClass) {
    this.typeClass = typeClass;
    this.typeDtoClass = typeDtoClass;
  }

  /**
   * Retorna a classe do tipo DTO gerenciado por este controlador.
   *
   * @return a classe do DTO associado ao controlador
   */
  public Class<D> getTypeDtoClass() {
    return typeDtoClass;
  }

  /**
   * Converte uma entidade do tipo T em seu DTO correspondente do tipo D utilizando ModelMapper.
   *
   * @param entity entidade a ser convertida
   * @return DTO correspondente à entidade fornecida
   */
  private D convertToDto(T entity) {
    return getModelMapper().map(entity, this.typeDtoClass);
  }

  /**
   * Converte um DTO para a entidade correspondente do tipo gerenciado por este controlador.
   *
   * @param entityDto o DTO a ser convertido
   * @return a entidade resultante da conversão do DTO
   */
  protected T convertToEntity(D entityDto) {
    return getModelMapper().map(entityDto, this.typeClass);
  }

  /**
   * Retorna todos os registros convertidos para DTOs.
   *
   * @return ResponseEntity contendo a lista de DTOs e status HTTP 200 OK
   */
  @GetMapping
  @Operation(summary = "Retorna uma lista de todos os registros")
  public ResponseEntity<List<D>> findAll() {
    return ResponseEntity.ok(getService().findAll().stream().map(this::convertToDto).toList());
  }

  /**
   * Retorna uma página de DTOs das entidades, com suporte a paginação e ordenação opcionais.
   *
   * @param page número da página a ser retornada (iniciando em 0)
   * @param size quantidade de itens por página
   * @param order campo opcional para ordenação dos resultados
   * @param asc define se a ordenação é ascendente (true) ou descendente (false)
   * @return página contendo os DTOs conforme os critérios especificados
   */
  @GetMapping("page")
  @Operation(
      summary = "Retorna um paginável com os registros de acordo com os critérios fornecidos")
  public ResponseEntity<Page<D>> findAll(
      @RequestParam int page,
      @RequestParam int size,
      @RequestParam(required = false) String order,
      @RequestParam(required = false) Boolean asc) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (order != null && asc != null) {
      pageRequest =
          PageRequest.of(page, size, asc ? Sort.Direction.ASC : Sort.Direction.DESC, order);
    }
    return ResponseEntity.ok(getService().findAll(pageRequest).map(this::convertToDto));
  }

  /**
   * Busca uma entidade pelo identificador e retorna seu DTO correspondente.
   *
   * <p>Retorna HTTP 200 com o DTO se a entidade for encontrada, ou HTTP 404 se não for localizada.
   *
   * @param i identificador da entidade
   * @return ResponseEntity contendo o DTO da entidade ou status 404 se não encontrada
   */
  @GetMapping("{i}")
  @Operation(summary = "Retorna um registro de acordo com o identificador fornecido")
  public ResponseEntity<D> findOne(@PathVariable I i) {
    try {
      T entity = getService().findOne(i);
      return ResponseEntity.ok(convertToDto(entity));
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Cria uma nova entidade a partir do DTO fornecido e retorna o DTO correspondente persistido.
   *
   * @param entity DTO validado com os dados para criação da entidade
   * @return ResponseEntity contendo o DTO salvo e status HTTP 201 Created
   */
  @PostMapping
  @Operation(summary = "Cria um novo registro com os dados fornecidos")
  public ResponseEntity<D> create(@RequestBody @Valid D entity) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(convertToDto(getService().save(convertToEntity(entity))));
  }

  /**
   * Atualiza uma entidade existente com os dados do DTO fornecido, garantindo que o identificador
   * do caminho seja igual ao do DTO.
   *
   * <p>Retorna HTTP 400 se os identificadores forem diferentes.
   *
   * @param i identificador da entidade no caminho da requisição
   * @param entity DTO com os dados atualizados
   * @return ResponseEntity contendo o DTO atualizado e status 200 em caso de sucesso, ou status 400
   *     se houver divergência de identificadores
   */
  @PutMapping("{i}")
  @Operation(summary = "Atualiza um registro de acordo com o identificador fornecido")
  public ResponseEntity<D> update(@PathVariable I i, @RequestBody @Valid D entity) {
    T entityToUpdate = convertToEntity(entity);
    I entityI = (I) entityToUpdate.getId();
    if (!i.equals(entityI)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    return ResponseEntity.status(HttpStatus.OK)
        .body(convertToDto(getService().save(entityToUpdate)));
  }

  /**
   * Verifica se existe uma entidade com o identificador especificado.
   *
   * @param i identificador da entidade a ser verificada
   * @return ResponseEntity contendo true se a entidade existe, ou false caso contrário
   */
  @GetMapping("exists/{i}")
  @Operation(summary = "Verifica se um registro existe de acordo com o identificador fornecido")
  public ResponseEntity<Boolean> exists(@PathVariable I i) {
    return ResponseEntity.ok(getService().exists(i));
  }

  /**
   * Retorna a quantidade total de entidades existentes.
   *
   * @return ResponseEntity contendo o número total de registros cadastrados
   */
  @GetMapping("count")
  @Operation(summary = "Retorna a quantidade total de registros")
  public ResponseEntity<Long> count() {
    return ResponseEntity.ok(getService().count());
  }

  /**
   * Exclui a entidade correspondente ao identificador fornecido.
   *
   * <p>Sempre retorna HTTP 204 No Content, independentemente da existência prévia da entidade.
   *
   * @param i identificador da entidade a ser excluída
   * @return resposta HTTP 204 No Content
   */
  @DeleteMapping("{i}")
  @Operation(summary = "Exclui um registro de acordo com o identificador fornecido")
  public ResponseEntity<Void> delete(@PathVariable I i) {
    getService().delete(i);
    return ResponseEntity.noContent().build();
  }
}
