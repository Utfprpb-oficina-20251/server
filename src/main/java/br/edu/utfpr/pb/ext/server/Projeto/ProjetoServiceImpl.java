package br.edu.utfpr.pb.ext.server.Projeto;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjetoServiceImpl extends CrudServiceImpl<Projeto, Long> implements IProjetoService {
    private final ProjetoRepository projetoRepository;

    public ProjetoServiceImpl(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    @Override
    protected JpaRepository<Projeto, Long> getRepository() {
        return projetoRepository;
    }
}
