package br.edu.utfpr.pb.ext.server.generics;

import io.swagger.v3.oas.annotations.Operation;
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
   * Fornece a instância do serviço CRUD responsável pelas operações de persistência para a
   * entidade.
   *
   * @return implementação de ICrudService para a entidade e identificador especificados
   */
  protected abstract ICrudService<T, I> getService();

  /**
   * Retorna a instância de ModelMapper utilizada para conversão entre entidades e DTOs.
   *
   * @return ModelMapper para conversão de tipos no controlador CRUD.
   */
  protected abstract ModelMapper getModelMapper();

  private final Class<T> typeClass;
  private final Class<D> typeDtoClass;

  /**
   * Cria um controlador CRUD genérico para a entidade e DTO especificados.
   *
   * @param typeClass classe da entidade gerenciada pelo controlador
   * @param typeDtoClass classe do DTO correspondente à entidade
   */
  protected CrudController(Class<T> typeClass, Class<D> typeDtoClass) {
    this.typeClass = typeClass;
    this.typeDtoClass = typeDtoClass;
  }

  public Class<D> getTypeDtoClass() {
    return typeDtoClass;
  }

  /**
   * Converte uma entidade do tipo T para seu DTO correspondente do tipo D usando ModelMapper.
   *
   * @param entity a entidade a ser convertida
   * @return o DTO resultante da conversão
   */
  private D convertToDto(T entity) {
    return getModelMapper().map(entity, this.typeDtoClass);
  }

  /**
   * Converte um DTO em uma instância da entidade correspondente usando o ModelMapper.
   *
   * @param entityDto DTO a ser convertido.
   * @return Instância da entidade resultante da conversão.
   */
  private T convertToEntity(D entityDto) {
    return getModelMapper().map(entityDto, this.typeClass);
  }

  /**
   * Retorna uma lista de todos os registros convertidos para DTO.
   *
   * @return ResponseEntity contendo uma lista de DTOs e status HTTP 200 OK
   */
  @GetMapping
  @Operation(summary = "Retorna uma lista de todos os registros")
  public ResponseEntity<List<D>> findAll() {
    return ResponseEntity.ok(getService().findAll().stream().map(this::convertToDto).toList());
  }

  /**
   * Retorna uma página de entidades convertidas para DTOs, com suporte a paginação e ordenação
   * opcionais.
   *
   * @param page número da página a ser retornada (iniciando em 0)
   * @param size quantidade de itens por página
   * @param order (opcional) campo para ordenação
   * @param asc (opcional) define se a ordenação é ascendente (true) ou descendente (false)
   * @return página de DTOs correspondente aos critérios informados
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
   * Recupera uma entidade pelo seu identificador e retorna seu DTO correspondente.
   *
   * <p>Retorna HTTP 200 com o DTO se a entidade for encontrada, ou HTTP 204 se não existir.
   *
   * @param i identificador da entidade a ser buscada
   * @return ResponseEntity contendo o DTO da entidade ou status 204 se não encontrada
   */
  @GetMapping("{i}")
  @Operation(summary = "Retorna um registro de acordo com o identificador fornecido")
  public ResponseEntity<D> findOne(@PathVariable I i) {
    T entity = getService().findOne(i);
    if (entity != null) {
      return ResponseEntity.ok(convertToDto(entity));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Cria uma nova entidade a partir do DTO fornecido e retorna o DTO salvo.
   *
   * @param entity DTO da entidade a ser criada (validação aplicada)
   * @return ResponseEntity contendo o DTO salvo e status HTTP 201 Created
   */
  @PostMapping
  @Operation(summary = "Cria um novo registro com os dados fornecidos")
  public ResponseEntity<D> create(@RequestBody @Valid D entity) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(convertToDto(getService().save(convertToEntity(entity))));
  }

  /**
   * Atualiza uma entidade existente identificada pelo ID com os dados fornecidos no DTO.
   *
   * @param i identificador da entidade a ser atualizada
   * @param entity DTO contendo os novos dados para atualização
   * @return ResponseEntity com o DTO atualizado e status HTTP 200 OK
   */
  @PutMapping("{i}")
  @Operation(summary = "Atualiza um registro de acordo com o identificador fornecido")
  public ResponseEntity<D> update(@PathVariable I i, @RequestBody @Valid D entity) {
    T entityToUpdate = convertToEntity(entity);
    I entityI = (I) entityToUpdate.getId();
    if (!i.equals(entityI)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(null); // or provide an error message DTO if applicable
    }
    return ResponseEntity.status(HttpStatus.OK)
        .body(convertToDto(getService().save(entityToUpdate)));
  }

  /**
   * Verifica se uma entidade com o ID especificado existe.
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
   * Retorna a quantidade total de entidades.
   *
   * @return ResponseEntity contendo o número total de entidades cadastradas
   */
  @GetMapping("count")
  @Operation(summary = "Retorna a quantidade total de registros")
  public ResponseEntity<Long> count() {
    return ResponseEntity.ok(getService().count());
  }

  /**
   * Exclui a entidade identificada pelo ID fornecido.
   *
   * <p>Retorna HTTP 204 No Content após a exclusão, independentemente de a entidade existir ou não.
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
