package br.edu.utfpr.pb.ext.server.curso;

import br.edu.utfpr.pb.ext.server.generics.CrudServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CursoServiceImpl extends CrudServiceImpl<Curso, Long> implements CursoService {
    private final CursoRepository cursoRepository;

    /**
     * Cria uma nova instância do serviço de cursos com o repositório especificado.
     *
     * @param cursoRepository repositório utilizado para operações de persistência de cursos
     */
    public CursoServiceImpl(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    /**
     * Retorna o repositório JPA utilizado para operações CRUD da entidade Curso.
     *
     * @return o repositório de Curso
     */
    @Override
    protected JpaRepository<Curso, Long> getRepository() {
        return cursoRepository;
    }
}
