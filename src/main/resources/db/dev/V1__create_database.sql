CREATE TABLE tb_curso (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    codigo VARCHAR(100) NOT NULL UNIQUE
);
CREATE TABLE tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    registro VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefone VARCHAR(20),
    curso_id BIGINT,
    CONSTRAINT fk_usuario_curso FOREIGN KEY (curso_id) REFERENCES tb_curso(id)
);
CREATE TABLE tb_projeto (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    justificativa TEXT,
    data_inicio DATE,
    data_fim DATE,
    publico_alvo VARCHAR(255),
    vinculado_disciplina BOOLEAN NOT NULL DEFAULT FALSE,
    restricao_publico VARCHAR(255)
);
CREATE TABLE tb_equipe_servidor (
    id_projeto BIGINT NOT NULL,
    id_usuario BIGINT NOT NULL,
    PRIMARY KEY (id_projeto, id_usuario),
    CONSTRAINT fk_equipe_projeto FOREIGN KEY (id_projeto) REFERENCES tb_projeto(id),
    CONSTRAINT fk_equipe_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id)
);
CREATE TABLE tb_sujestao_de_projeto (
    id BIGSERIAL PRIMARY KEY,
    aluno_id BIGINT NOT NULL,
    professor_id BIGINT NOT NULL,
    curso_id BIGINT NOT NULL,
    descricao TEXT,
    CONSTRAINT fk_sujestao_aluno FOREIGN KEY (aluno_id) REFERENCES tb_usuario(id),
    CONSTRAINT fk_sujestao_professor FOREIGN KEY (professor_id) REFERENCES tb_usuario(id),
    CONSTRAINT fk_sujestao_curso FOREIGN KEY (curso_id) REFERENCES tb_curso(id)
);
CREATE TABLE tb_inscricao (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    projeto_id BIGINT NOT NULL,
    data_de_inscricao DATE NOT NULL,
    CONSTRAINT fk_inscricao_usuario FOREIGN KEY (usuario_id) REFERENCES tb_usuario(id),
    CONSTRAINT fk_inscricao_projeto FOREIGN KEY (projeto_id) REFERENCES tb_projeto(id)
);