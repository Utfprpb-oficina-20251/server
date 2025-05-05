package br.edu.utfpr.pb.ext.server.Projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudController;
import br.edu.utfpr.pb.ext.server.generics.ICrudService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projetos")
public class ProjetoController extends CrudController<Projeto, ProjetoDTO, Long> {

    private final IProjetoService projetoService;
    private final ModelMapper modelMapper;

    public ProjetoController(IProjetoService projetoService, ModelMapper modelMapper) {
        super(Projeto.class, ProjetoDTO.class);
        this.projetoService = projetoService;
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
}
