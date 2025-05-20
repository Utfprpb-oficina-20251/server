-- =============================================================
-- Script: V1__create_tb_access_code.sql
-- Descrição: Criação da tabela de códigos temporários enviados por e-mail
-- Autor: Luiz Alberto dos Passos
-- Data: 2025-05-19
-- =============================================================

-- 1. Remove a tabela se já existir (apenas para ambiente de testes ou reset)
-- DROP TABLE IF EXISTS tb_access_code;

-- 2. Criação da tabela principal
CREATE TABLE IF NOT EXISTS tb_access_code (
    id BIGSERIAL PRIMARY KEY, -- Chave primária auto-incrementável
    email VARCHAR(255) NOT NULL, -- E-mail destinatário
    code VARCHAR(255) NOT NULL UNIQUE, -- Código único
    generated_at TIMESTAMP NOT NULL, -- Data/hora de geração do código
    used BOOLEAN NOT NULL DEFAULT FALSE, -- Se já foi utilizado
    expiration TIMESTAMP NOT NULL, -- Data/hora de expiração
    type VARCHAR(255) NOT NULL -- Tipo do código: cadastro, recuperação, etc.
    );

-- 3. Índices adicionais (melhoram buscas por e-mail e tipo)
CREATE INDEX IF NOT EXISTS idx_tb_access_code_email ON tb_access_code(email);
CREATE INDEX IF NOT EXISTS idx_tb_access_code_type ON tb_access_code(type);

-- =============================================================
-- Observações:
-- - Esta tabela é utilizada para validar ações como cadastro ou recuperação de senha
-- - A aplicação deve gerenciar a expiração e o uso dos códigos
-- =============================================================