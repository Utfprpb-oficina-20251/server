CREATE TABLE tb_notificacao
(
    id               BIGSERIAL PRIMARY KEY,
    titulo           VARCHAR(100) NOT NULL,
    descricao        TEXT         NOT NULL,
    tipo_notificacao VARCHAR(50)  NOT NULL,
    tipo_referencia  VARCHAR(50),
    referencia_id    BIGINT,
    data_criacao     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lida             BOOLEAN      NOT NULL DEFAULT FALSE,
    usuario_id       BIGINT       NOT NULL,

    CONSTRAINT fk_notificacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES tb_usuario (id) ON DELETE CASCADE
);

-- Índices para otimizar consultas
CREATE INDEX idx_notificacao_usuario_id ON tb_notificacao (usuario_id);
CREATE INDEX idx_notificacao_lida ON tb_notificacao (lida);
CREATE INDEX idx_notificacao_data_criacao ON tb_notificacao (data_criacao DESC);
CREATE INDEX idx_notificacao_usuario_lida ON tb_notificacao (usuario_id, lida);
