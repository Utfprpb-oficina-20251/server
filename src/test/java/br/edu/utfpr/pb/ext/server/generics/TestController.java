package br.edu.utfpr.pb.ext.server.generics;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/test")
public class TestController extends CrudController<TestEntity, TestDTO, Long> {

  private final ICrudService service;
  private final ModelMapper modelMapper;

  public TestController(ICrudService service, ModelMapper modelMapper) {
    super(TestEntity.class, TestDTO.class);
    this.service = service;
    this.modelMapper = modelMapper;
  }

  @Override
  protected ICrudService<TestEntity, Long> getService() {
    return this.service;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }
}
