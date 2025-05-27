package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.departamento.enums.Departamentos;
import br.edu.utfpr.pb.ext.server.departamento.impl.DepartamentoServiceImpl;
import br.edu.utfpr.pb.ext.server.usuario.Usuario;
import br.edu.utfpr.pb.ext.server.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DepartamentoServiceImpl.
 * Valida as operações principais da camada de serviço:
 * salvar, listar todos e buscar por nome do departamento.
 */
public class DepartamentoServiceTest {

    private DepartamentoRepository departamentoRepository;
    private UsuarioRepository usuarioRepository;
    private DepartamentoServiceImpl service;

    @BeforeEach
    void setup() {
        departamentoRepository = mock(DepartamentoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new DepartamentoServiceImpl(departamentoRepository, usuarioRepository);
    }

    @Test
    void deveSalvarDepartamentoComResponsavel() {
        DepartamentoDTO dto = new DepartamentoDTO();
        dto.setDepartamento(Departamentos.DAINF);
        dto.setResponsavelId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Departamento entity = new Departamento();
        entity.setId(10L);
        entity.setDepartamento(Departamentos.DAINF);
        entity.setResponsavel(usuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(departamentoRepository.save(any())).thenReturn(entity);

        DepartamentoDTO salvo = service.save(dto);

        assertNotNull(salvo);
        assertEquals(Departamentos.DAINF, salvo.getDepartamento());
        assertEquals(1L, salvo.getResponsavelId());
    }

    @Test
    void deveListarTodosDepartamentos() {
        Usuario usuario = new Usuario();
        usuario.setId(2L);

        Departamento entity = new Departamento();
        entity.setId(20L);
        entity.setDepartamento(Departamentos.DAAGR);
        entity.setResponsavel(usuario);

        when(departamentoRepository.findAll()).thenReturn(List.of(entity));

        List<DepartamentoDTO> lista = service.findAll();

        assertEquals(1, lista.size());
        assertEquals(Departamentos.DAAGR, lista.get(0).getDepartamento());
        assertEquals(2L, lista.get(0).getResponsavelId());
    }

    @Test
    void deveBuscarDepartamentoPorNome() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);

        Departamento entity = new Departamento();
        entity.setId(30L);
        entity.setDepartamento(Departamentos.DAMEC);
        entity.setResponsavel(usuario);

        when(departamentoRepository.findByDepartamento(Departamentos.DAMEC))
                .thenReturn(Optional.of(entity));

        DepartamentoDTO dto = service.findByDepartamento("DAMEC");

        assertNotNull(dto);
        assertEquals(3L, dto.getResponsavelId());
        assertEquals(Departamentos.DAMEC, dto.getDepartamento());
    }
}