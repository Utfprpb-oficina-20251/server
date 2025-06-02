-- Adiciona coluna responsavel_id à tabela tb_departamento
ALTER TABLE tb_departamento
ADD COLUMN responsavel_id BIGINT;

-- Cria chave estrangeira para a tabela de usuários
ALTER TABLE tb_departamento
ADD CONSTRAINT fk_departamento_responsavel
FOREIGN KEY (responsavel_id)
REFERENCES tb_usuario(id);