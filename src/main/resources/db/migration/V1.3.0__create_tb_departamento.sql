DROP TABLE IF EXISTS tb_departamento;

CREATE TABLE tb_departamento (
                                 id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                 sigla VARCHAR(20) NOT NULL UNIQUE,
                                 nome VARCHAR(255) NOT NULL
);

INSERT INTO tb_departamento (sigla, nome) VALUES
                                              ('CALEM', 'Centro Acadêmico Lingua Estrangeira Moderna'),
                                              ('DAADM', 'Departamento de Administração'),
                                              ('DAGRO', 'Departamento de Agronomia'),
                                              ('DACOC', 'Departamento Acadêmico de Construção Civil'),
                                              ('DACON', 'Departamento de Ciências Contábeis'),
                                              ('DAELE', 'Departamento de Engenharia Elétrica'),
                                              ('DAFIS', 'Departamento de Física'),
                                              ('DAHUM', 'Departamento de Ciências Humanas'),
                                              ('DAINF', 'Departamento de Ciência da Computação'),
                                              ('DALET', 'Departamento de Letras'),
                                              ('DAMAT', 'Departamento de Matemática'),
                                              ('DAMEC', 'Departamento de Engenharia Mecânica'),
                                              ('DAQUI', 'Departamento de Química'),
                                              ('LATO_SENSU', 'Departamento de Pós-Graduação Lato Sensu'),
                                              ('NUAPE', 'Núcleo de Apoio Pedagógico'),
                                              ('STRICTO', 'Departamento de Pós-Graduação Stricto Sensu');