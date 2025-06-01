package br.edu.utfpr.pb.ext.server.generics;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/test")
public class TestController extends CrudController<TestEntity, TestDTO, Long> {

  private final ICrudService<TestEntity, Long> service;
  private final ModelMapper modelMapper;

  /**
   * Cria uma instância de TestController com o serviço CRUD e o ModelMapper fornecidos.
   *
   * @param service implementação do serviço CRUD para TestEntity
   * @param modelMapper instância do ModelMapper para conversão entre entidades e DTOs
   */
  public TestController(ICrudService<TestEntity, Long> service, ModelMapper modelMapper) {
    super(TestEntity.class, TestDTO.class);
    this.service = service;
    this.modelMapper = modelMapper;
  }

  /**
   * Retorna a instância do serviço CRUD utilizada por este controlador.
   *
   * @return o serviço CRUD para TestEntity
   */
  @Override
  protected ICrudService<TestEntity, Long> getService() {
    return this.service;
  }

  /**
   * Retorna a instância de ModelMapper utilizada pelo controlador.
   *
   * @return o ModelMapper configurado para conversão entre entidades e DTOs
   */
  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }
}
