package br.edu.utfpr.pb.ext.server.departamento;

import br.edu.utfpr.pb.ext.server.generics.ICrudService;

/**
 * Interface de serviço para operações relacionadas à entidade Departamento.
 * Estende a interface genérica ICrudService, herdando operações básicas de CRUD.
 *
 * Essa interface pode ser expandida futuramente com métodos específicos
 * relacionados a regras de negócio da entidade Departamento.
 */
public interface DepartamentoService extends ICrudService<Departamento, Long> {}