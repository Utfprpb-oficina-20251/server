CREATE TABLE usuario_roles (
                               usuario_id BIGINT NOT NULL,
                               role VARCHAR(255) NOT NULL,
                               PRIMARY KEY (usuario_id, role),
                               CONSTRAINT fk_usuario_roles_usuario
                                   FOREIGN KEY (usuario_id)
                                       REFERENCES tb_usuario (id)
                                       ON DELETE CASCADE
);