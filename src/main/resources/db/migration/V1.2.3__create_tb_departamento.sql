CREATE TABLE IF NOT EXISTS tb_departamento (
                                               id BIGINT PRIMARY KEY,
                                               sigla VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL
    );

-- Inserts com proteção para evitar duplicidade
INSERT INTO tb_departamento (sigla, nome)
SELECT 'CALEM', 'Centro Acadêmico Lingua Estrangeira Moderna'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'CALEM');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAADM', 'Departamento de Administração'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAADM');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAGRO', 'Departamento de Agronomia'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAGRO');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DACOC', 'Departamento Acadêmico de Construção Civil'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DACOC');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DACON', 'Departamento de Ciências Contábeis'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DACON');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAELE', 'Departamento de Engenharia Elétrica'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAELE');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAFIS', 'Departamento de Física'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAFIS');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAHUM', 'Departamento de Ciências Humanas'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAHUM');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAINF', 'Departamento de Ciência da Computação'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAINF');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DALET', 'Departamento de Letras'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DALET');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAMAT', 'Departamento de Matemática'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAMAT');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAMEC', 'Departamento de Engenharia Mecânica'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAMEC');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'DAQUI', 'Departamento de Química'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'DAQUI');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'LATO_SENSU', 'Departamento de Pós-Graduação Lato Sensu'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'LATO_SENSU');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'NUAPE', 'Núcleo de Apoio Pedagógico'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'NUAPE');

INSERT INTO tb_departamento (sigla, nome)
SELECT 'STRICTO', 'Departamento de Pós-Graduação Stricto Sensu'
    WHERE NOT EXISTS (SELECT 1 FROM tb_departamento WHERE sigla = 'STRICTO');