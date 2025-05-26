ALTER TABLE tb_sugestao_de_projeto ADD COLUMN titulo VARCHAR(100) NOT NULL;
ALTER TABLE tb_sugestao_de_projeto ADD COLUMN publico_alvo VARCHAR(500) NOT NULL;
ALTER TABLE tb_sugestao_de_projeto ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO';
ALTER TABLE tb_sugestao_de_projeto ADD COLUMN data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE tb_sugestao_de_projeto ALTER COLUMN professor_id DROP NOT NULL;