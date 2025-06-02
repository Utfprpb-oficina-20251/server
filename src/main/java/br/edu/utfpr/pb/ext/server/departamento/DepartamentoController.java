package br.edu.utfpr.pb.ext.server.departamento;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller responsável pelos endpoints da entidade Departamento.
 * Fornece operações de CRUD (Create, Read, Update, Delete).
 */
@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

  private final DepartamentoService service;

  /**
   * Construtor com injeção de dependência do serviço de departamento.
   */
  public DepartamentoController(DepartamentoService service) {
    this.service = service;
  }

  /**
   * Lista todos os departamentos cadastrados.
   * @return Lista de DepartamentoDto com status HTTP 200 (OK).
   */
  @GetMapping
  public ResponseEntity<List<DepartamentoDto>> listarTodos() {
    List<DepartamentoDto> dtos = service.findAll().stream()
            .map(this::toDto) // Conversão de entidade para DTO
            .collect(Collectors.toList());
    return ResponseEntity.ok(dtos);
  }

  /**
   * Busca um departamento pelo ID informado na URL.
   * @param id ID do departamento.
   * @return DepartamentoDto correspondente com status HTTP 200.
   */
  @GetMapping("/{id}")
  public ResponseEntity<DepartamentoDto> buscarPorId(@PathVariable Long id) {
    Departamento departamento = service.findOne(id);
    return ResponseEntity.ok(toDto(departamento));
  }

  /**
   * Cria um novo departamento com base no DTO recebido no corpo da requisição.
   * @param dto DTO com os dados do novo departamento.
   * @return DepartamentoDto criado com status HTTP 201 (Created).
   */
  @PostMapping
  public ResponseEntity<DepartamentoDto> criar(@RequestBody DepartamentoDto dto) {
    Departamento salvo = service.save(toEntity(dto));
    return ResponseEntity.status(201).body(toDto(salvo));
  }

  /**
   * Atualiza os dados de um departamento existente.
   * @param id ID do departamento a ser atualizado.
   * @param dto DTO com os novos dados.
   * @return DepartamentoDto atualizado com status HTTP 200, ou 400 se o ID for inconsistente.
   */
  @PutMapping("/{id}")
  public ResponseEntity<DepartamentoDto> atualizar(@PathVariable Long id, @RequestBody DepartamentoDto dto) {
    if (!id.equals(dto.getId())) {
      return ResponseEntity.badRequest().build(); // Garante consistência entre URL e corpo da requisição
    }
    Departamento atualizado = service.save(toEntity(dto));
    return ResponseEntity.ok(toDto(atualizado));
  }

  /**
   * Exclui um departamento com base no ID fornecido.
   * @param id ID do departamento a ser excluído.
   * @return Resposta sem conteúdo (HTTP 204).
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  // Conversão auxiliar de entidade para DTO
  private DepartamentoDto toDto(Departamento entity) {
    DepartamentoDto dto = new DepartamentoDto();
    dto.setId(entity.getId());
    dto.setNome(entity.getNome());
    dto.setSigla(entity.getSigla());
    if (entity.getResponsavel() != null) {
      dto.setResponsavelId(entity.getResponsavel().getId());
    }
    return dto;
  }

  // Conversão auxiliar de DTO para entidade
  private Departamento toEntity(DepartamentoDto dto) {
    Departamento entity = new Departamento();
    entity.setId(dto.getId());
    entity.setNome(dto.getNome());
    entity.setSigla(dto.getSigla());
    return entity;
  }

  /**
   * Associa um usuário como responsável por um departamento.
   * @param id ID do departamento.
   * @param usuarioId ID do usuário que será associado como responsável.
   * @return Resposta HTTP 200 (OK) em caso de sucesso.
   */
  @PutMapping("/{id}/responsavel/{usuarioId}")
  public ResponseEntity<Void> associarResponsavel(@PathVariable Long id, @PathVariable Long usuarioId) {
    service.associarResponsavel(id, usuarioId);
    return ResponseEntity.ok().build();
  }
}