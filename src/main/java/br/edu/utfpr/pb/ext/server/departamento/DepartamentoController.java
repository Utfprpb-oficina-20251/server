package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller responsável por expor endpoints relacionados à associação de responsáveis aos
 * departamentos.
 */
@RestController
@RequestMapping("/api/departamentos-responsaveis")
@RequiredArgsConstructor
public class DepartamentoController {

  // Serviço que contém as regras de negócio para a entidade Departamento
  private final DepartamentoService service;

  /**
   * Endpoint para salvar (criar ou atualizar) uma associação entre um departamento e um
   * responsável.
   *
   * @param dto DTO contendo os dados da associação
   * @return DTO da associação salva
   */
  @PostMapping
  public ResponseEntity<DepartamentoDTO> salvar(
      @RequestBody DepartamentoDTO dto) {
    return ResponseEntity.ok(service.save(dto));
  }

  /**
   * Endpoint para listar todas as associações cadastradas entre departamentos e responsáveis.
   *
   * @return Lista de DTOs com todas as associações
   */
  @GetMapping
  public ResponseEntity<List<DepartamentoDTO>> listarTodos() {
    return ResponseEntity.ok(service.findAll());
  }

  /**
   * Endpoint para buscar a associação de um departamento pelo nome.
   *
   * @param nome Nome do departamento (enum {@link Departamentos})
   * @return DTO da associação encontrada
   */
  @GetMapping("/{nome}")
  public ResponseEntity<DepartamentoDTO> buscarPorNome(@PathVariable String nome) {
    return ResponseEntity.ok(service.findByDepartamento(nome));
  }
}
