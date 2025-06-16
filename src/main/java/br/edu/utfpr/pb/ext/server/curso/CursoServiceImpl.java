package br.edu.utfpr.pb.ext.server.curso;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CursoServiceImpl extends CrudServiceImpl<Curso, Long> implements CursoService {
    private final CursoRepository cursoRepository;

    public CursoServiceImpl(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    protected JpaRepository<Curso, Long> getRepository() {
        return cursoRepository;
    }
}
