package br.edu.utfpr.pb.ext.server.Usuario;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController extends CrudController<Usuario, UsuarioDTO, Long> {
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public UsuarioController(IUsuarioService usuarioService, ModelMapper modelMapper) {
        super(Usuario.class, UsuarioDTO.class);
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }

    @Override
    protected ICrudService<Usuario, Long> getService() {
        return usuarioService;
    }

    @Override
    protected ModelMapper getModelMapper() {
        return modelMapper;
    }
}
