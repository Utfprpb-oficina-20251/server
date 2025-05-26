package br.edu.utfpr.pb.ext.server.sugestaoprojeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.dto.SugestaoDeProjetoDTO;
import br.edu.utfpr.pb.ext.server.sugestaoprojeto.service.SugestaoDeProjetoServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sugestao")
@Tag(
    name = "SugestaoDeProjetoController",
    description = "Endpoints para gerenciar sugestões de projeto")
public class SugestaoDeProjetoController
    extends CrudController<SugestaoDeProjeto, SugestaoDeProjetoDTO, Long> {

  private final SugestaoDeProjetoServiceImpl service;
  private final ModelMapper modelMapper;

  public SugestaoDeProjetoController(
      SugestaoDeProjetoServiceImpl service, ModelMapper modelMapper) {
    super(SugestaoDeProjeto.class, SugestaoDeProjetoDTO.class);
    this.service = service;
    this.modelMapper = modelMapper;
  }

  @Override
  protected ICrudService<SugestaoDeProjeto, Long> getService() {
    return this.service;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

  @GetMapping("/minhas-sugestoes")
  public ResponseEntity<List<SugestaoDeProjetoDTO>> listarSugestoesDoUsuarioLogado() {
    List<SugestaoDeProjeto> sugestoes = service.listarSugestoesDoUsuarioLogado();
    return ResponseEntity.ok(sugestoes.stream().map(this::convertToResponseDTO).toList());
  }

  private SugestaoDeProjetoDTO convertToResponseDTO(SugestaoDeProjeto sugestaoDeProjeto) {
    return getModelMapper().map(sugestaoDeProjeto, SugestaoDeProjetoDTO.class);
  }
}
