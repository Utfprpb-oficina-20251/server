ALTER TABLE tb_usuario DROP COLUMN departamento;

ALTER TABLE tb_usuario ADD COLUMN departamento_id BIGINT;
ALTER TABLE tb_usuario ADD CONSTRAINT fk_usuario_departamento FOREIGN KEY (departamento_id) REFERENCES tb_departamento(id);