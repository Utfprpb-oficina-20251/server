package br.edu.utfpr.pb.ext.server.departamento;

import java.util.List;

/**
 * Interface de serviço responsável pelas operações de negócio
 * relacionadas à entidade Departamento.
 */
public interface DepartamentoService {

    /**
     * Retorna uma lista com todos os departamentos cadastrados.
     *
     * @return Lista de departamentos.
     */
    List<Departamento> findAll();

    /**
     * Retorna um departamento com base no seu ID.
     *
     * @param id Identificador do departamento.
     * @return Instância da entidade Departamento correspondente.
     */
    Departamento findOne(Long id);

    /**
     * Salva ou atualiza um departamento.
     *
     * @param departamento Entidade a ser salva.
     * @return Entidade persistida.
     */
    Departamento save(Departamento departamento);

    /**
     * Exclui um departamento com base no ID.
     *
     * @param id Identificador do departamento a ser excluído.
     */
    void delete(Long id);

    /**
     * Associa um usuário como responsável por um departamento.
     *
     * @param departamentoId ID do departamento.
     * @param usuarioId      ID do usuário a ser associado como responsável.
     */
    void associarResponsavel(Long departamentoId, Long usuarioId);

}