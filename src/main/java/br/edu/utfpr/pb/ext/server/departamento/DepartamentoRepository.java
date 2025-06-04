package br.edu.utfpr.pb.ext.server.departamento;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA para a entidade Departamento.
 * Fornece operações básicas de persistência, como salvar, buscar, atualizar e deletar.
 *
 * A interface JpaRepository já fornece métodos prontos como:
 * - findById
 * - findAll
 * - save
 * - deleteById
 * - existsById
 */
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {}