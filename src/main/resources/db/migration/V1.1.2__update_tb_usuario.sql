ALTER TABLE tb_usuario RENAME COLUMN registro TO cpf;

ALTER TABLE tb_usuario ADD COLUMN siape VARCHAR(7);
ALTER TABLE tb_usuario ADD COLUMN registro_academico VARCHAR(9);
ALTER TABLE tb_usuario ADD COLUMN departamento VARCHAR(50);

ALTER TABLE tb_usuario ADD CONSTRAINT uq_usuario_cpf UNIQUE (cpf);
ALTER TABLE tb_usuario ADD CONSTRAINT uq_usuario_siape UNIQUE (siape);
ALTER TABLE tb_usuario ADD CONSTRAINT uq_usuario_registro_academico UNIQUE (registro_academico);
