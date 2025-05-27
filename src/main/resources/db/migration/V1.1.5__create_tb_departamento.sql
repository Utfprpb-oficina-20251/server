CREATE TABLE tb_departamento (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     departamento VARCHAR(100) NOT NULL UNIQUE,
     usuario_id BIGINT NOT NULL,
     FOREIGN KEY (usuario_id) REFERENCES tb_usuario(id)
);