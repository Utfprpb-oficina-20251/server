package br.edu.utfpr.pb.ext.server.curso;

import br.edu.utfpr.pb.ext.server.curso.dto.CursoDTO;
import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cursos", description = "Endpoints relacionados a manutenção de cursos")
@RestController
@RequestMapping("/curso")
public class CursoController extends CrudController<Curso, CursoDTO, Long> {
  private final ModelMapper modelMapper;
  private final CursoService cursoService;

  public CursoController(ModelMapper modelMapper, CursoService cursoService) {
    super(Curso.class, CursoDTO.class);
    this.modelMapper = modelMapper;
    this.cursoService = cursoService;
  }

  @Override
  protected ICrudService<Curso, Long> getService() {
    return this.cursoService;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }
}
