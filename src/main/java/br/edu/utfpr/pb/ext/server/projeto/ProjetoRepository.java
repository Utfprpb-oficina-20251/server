package br.edu.utfpr.pb.ext.server.projeto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjetoRepository
    extends JpaRepository<Projeto, Long>, JpaSpecificationExecutor<Projeto> {}
