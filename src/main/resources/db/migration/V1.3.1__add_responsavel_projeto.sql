ALTER TABLE tb_projeto
    ADD COLUMN responsavel_id BIGINT;

ALTER TABLE tb_projeto
    ADD CONSTRAINT fk_projeto_responsavel FOREIGN KEY (responsavel_id) REFERENCES tb_usuario(id);
