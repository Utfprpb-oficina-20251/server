
CREATE TABLE tb_candidatura (
                                id BIGSERIAL PRIMARY KEY,
                                projeto_id BIGINT NOT NULL,
                                aluno_id BIGINT NOT NULL,
                                data_candidatura TIMESTAMP NOT NULL,
                                status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',

                                CONSTRAINT fk_candidatura_projeto FOREIGN KEY (projeto_id)
                                    REFERENCES tb_projeto (id) ON DELETE CASCADE,

                                CONSTRAINT fk_candidatura_aluno FOREIGN KEY (aluno_id)
                                    REFERENCES tb_usuario (id) ON DELETE CASCADE,

                                CONSTRAINT uq_candidatura UNIQUE (projeto_id, aluno_id)
);
