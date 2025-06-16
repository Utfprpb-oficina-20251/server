package br.edu.utfpr.pb.ext.server.curso;

import br.edu.utfpr.pb.ext.server.curso.dto.CursoDTO;
import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller para gerenciamento de Cursos. Disponibiliza endpoints REST seguindo o CRUD genérico.
 */
@RestController
@RequestMapping("/cursos")
@Tag(name = "Curso", description = "Endpoints para gerenciamento de cursos")
public class CursoController extends CrudController<Curso, CursoDTO, Long> {

  private final CursoService cursoService;
  private final ModelMapper modelMapper;

  /**
   * Construtor que injeta o serviço de Curso e o ModelMapper.
   *
   * @param cursoService serviço responsável pelas operações de Curso
   * @param modelMapper instância para mapeamento entre entidade e DTO
   */
  public CursoController(CursoService cursoService, ModelMapper modelMapper) {
    super(Curso.class, CursoDTO.class);
    this.cursoService = cursoService;
    this.modelMapper = modelMapper;
  }

  /**
   * Fornece o serviço específico para a entidade Curso.
   *
   * @return instância de CursoService
   */
  @Override
  protected ICrudService<Curso, Long> getService() {
    return cursoService;
  }

  /**
   * Fornece a instância de ModelMapper usada para conversão entre entidades e DTOs.
   *
   * @return instância de ModelMapper
   */
  @Override
  protected ModelMapper getModelMapper() {
    return modelMapper;
  }
}
