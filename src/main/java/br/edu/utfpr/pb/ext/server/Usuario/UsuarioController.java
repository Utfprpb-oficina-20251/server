package br.edu.utfpr.pb.ext.server.usuario;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.pb.ext.server.usuario.dto.UsuarioServidorResponseDTO;


@RestController
@RequestMapping("/usuarios")
public class UsuarioController extends CrudController<Usuario, UsuarioServidorResponseDTO, Long> {
    private final IUsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public UsuarioController(IUsuarioService usuarioService, ModelMapper modelMapper) {
        super(Usuario.class, UsuarioServidorResponseDTO.class);
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
