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
   * Constrói um controlador CRUD genérico para a entidade e DTO informados.
   *
   * @param typeClass classe da entidade que será gerenciada
   * @param typeDtoClass classe do DTO associado à entidade
   */
  protected CrudController(Class<T> typeClass, Class<D> typeDtoClass) {
    this.typeClass = typeClass;
    this.typeDtoClass = typeDtoClass;
  }

  /**
   * Retorna a classe do tipo DTO associado ao controlador.
   *
   * @return a classe correspondente ao tipo DTO
   */
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
   * Converte um DTO em uma entidade do tipo gerenciado pelo controlador.
   *
   * @param entityDto objeto DTO a ser convertido
   * @return entidade correspondente ao DTO fornecido
   */
  protected T convertToEntity(D entityDto) {
    return getModelMapper().map(entityDto, this.typeClass);
  }

  /**
   * Recupera todos os registros e os retorna convertidos para DTOs.
   *
   * @return ResponseEntity com a lista de DTOs e status HTTP 200 OK
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
   * @param page número da página a ser retornada (começando em 0)
   * @param size quantidade de itens por página
   * @param order campo opcional para ordenação dos resultados
   * @param asc define se a ordenação é ascendente (true) ou descendente (false)
   * @return página de DTOs conforme os critérios de paginação e ordenação especificados
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
   * <p>Retorna HTTP 200 com o DTO se a entidade for encontrada, ou HTTP 404 se não existir.
   *
   * @param i identificador da entidade a ser buscada
   * @return ResponseEntity contendo o DTO da entidade ou status 404 se não encontrada
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
   * Cria uma nova entidade com base no DTO fornecido e retorna o DTO persistido.
   *
   * @param entity DTO representando os dados da nova entidade (validação aplicada)
   * @return ResponseEntity com o DTO salvo e status HTTP 201 Created
   */
  @PostMapping
  @Operation(summary = "Cria um novo registro com os dados fornecidos")
  public ResponseEntity<D> create(@RequestBody @Valid D entity) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(convertToDto(getService().save(convertToEntity(entity))));
  }

  /**
   * Atualiza uma entidade existente com base no identificador fornecido e nos dados do DTO.
   *
   * <p>Retorna HTTP 400 se o ID do caminho não corresponder ao ID do DTO.
   *
   * @param i identificador da entidade a ser atualizada
   * @param entity DTO com os dados atualizados
   * @return ResponseEntity contendo o DTO atualizado e status 200 em caso de sucesso, ou 400 em
   *     caso de inconsistência de IDs
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
   * Retorna se existe uma entidade com o identificador fornecido.
   *
   * @param i identificador da entidade
   * @return ResponseEntity com true se a entidade existe, ou false caso contrário
   */
  @GetMapping("exists/{i}")
  @Operation(summary = "Verifica se um registro existe de acordo com o identificador fornecido")
  public ResponseEntity<Boolean> exists(@PathVariable I i) {
    return ResponseEntity.ok(getService().exists(i));
  }

  /**
   * Retorna o número total de entidades cadastradas.
   *
   * @return ResponseEntity com a contagem total de entidades
   */
  @GetMapping("count")
  @Operation(summary = "Retorna a quantidade total de registros")
  public ResponseEntity<Long> count() {
    return ResponseEntity.ok(getService().count());
  }

  /**
   * Remove a entidade correspondente ao identificador fornecido.
   *
   * <p>Retorna HTTP 204 No Content após a tentativa de exclusão, independentemente da existência da
   * entidade.
   *
   * @param i identificador da entidade a ser removida
   * @return resposta HTTP 204 No Content
   */
  @DeleteMapping("{i}")
  @Operation(summary = "Exclui um registro de acordo com o identificador fornecido")
  public ResponseEntity<Void> delete(@PathVariable I i) {
    getService().delete(i);
    return ResponseEntity.noContent().build();
  }
}
