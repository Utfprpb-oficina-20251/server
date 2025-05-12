package br.edu.utfpr.pb.ext.server.projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import br.edu.utfpr.pb.ext.server.projeto.enums.StatusProjeto;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioProjetoDTO;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("projeto")
public class ProjetoController extends CrudController<Projeto, ProjetoDTO, Long> {

  private final IProjetoService projetoService;
  private final UsuarioRepository usuarioRepository;
  private final ModelMapper modelMapper;

  public ProjetoController(IProjetoService projetoService, ModelMapper modelMapper,
                           UsuarioRepository usuarioRepository) {
    super(Projeto.class, ProjetoDTO.class);
    this.projetoService = projetoService;
    this.usuarioRepository = usuarioRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  protected ICrudService<Projeto, Long> getService() {
    return this.projetoService;
  }

  @Override
  protected ModelMapper getModelMapper() {
    return this.modelMapper;
  }

  @Override
  @PostMapping
  public ResponseEntity<ProjetoDTO> create
          (@Valid @RequestBody ProjetoDTO dto) {
    Projeto projeto = new Projeto();
    List<String> emails = dto.getEquipeExecutora().stream().map(UsuarioProjetoDTO::getEmail).toList();
    if (emails.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
    }
    ArrayList<Optional<Usuario>> usuarios = new ArrayList<>();
    for (String email : emails) {
      Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
      if (usuario.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
      }
      usuarios.add(usuario);
    }
    projeto.setTitulo(dto.getTitulo());
    projeto.setDescricao(dto.getDescricao());
    projeto.setJustificativa(dto.getJustificativa());
    projeto.setDataInicio(dto.getDataInicio());
    projeto.setDataFim(dto.getDataFim());
    projeto.setPublicoAlvo(dto.getPublicoAlvo());
    projeto.setVinculadoDisciplina(dto.isVinculadoDisciplina());
    projeto.setRestricaoPublico(dto.getRestricaoPublico());
    projeto.setEquipeExecutora(usuarios.stream().filter(Optional::isPresent).map(Optional::get).toList());
    projeto.setStatus(StatusProjeto.EM_ANDAMENTO);
    Projeto projetoResponse = projetoService.save(projeto);
    ProjetoDTO projetoDTO = modelMapper.map(projetoResponse, ProjetoDTO.class);
    return ResponseEntity.status(HttpStatus.CREATED).body(
            projetoDTO
    );
  }
}
