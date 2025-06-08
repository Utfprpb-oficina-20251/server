package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController extends CrudController<Departamento, DepartamentoDto, Long> {

  private final DepartamentoService departamentoService;
  private final ModelMapper modelMapper;

  public DepartamentoController(DepartamentoService departamentoService, ModelMapper modelMapper) {
    super(Departamento.class, DepartamentoDto.class); // informa as classes para o CRUD genérico
    this.departamentoService = departamentoService;
    this.modelMapper = modelMapper;
  }

  @Override
  protected DepartamentoService getService() {
    return departamentoService;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return modelMapper;
  }

  @PutMapping("/{id}/responsavel/{usuarioId}")
  public void associarResponsavel(@PathVariable Long id, @PathVariable Long usuarioId) {
    departamentoService.associarResponsavel(id, usuarioId);
  }
}
