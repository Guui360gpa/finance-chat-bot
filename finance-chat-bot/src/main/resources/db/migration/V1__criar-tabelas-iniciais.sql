CREATE TABLE usuario (
                         id           BIGSERIAL PRIMARY KEY,
                         telegram_id  BIGINT       NOT NULL UNIQUE,
                         nome         VARCHAR(100),
                         fuso_horario VARCHAR(40)  NOT NULL DEFAULT 'America/Sao_Paulo',
                         criado_em    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE categoria (
                           id         BIGSERIAL PRIMARY KEY,
                           usuario_id BIGINT      NOT NULL REFERENCES usuario(id),
                           nome       VARCHAR(50) NOT NULL,
                           UNIQUE (usuario_id, nome)
);

CREATE TABLE transacao (
                           id                BIGSERIAL PRIMARY KEY,
                           usuario_id        BIGINT         NOT NULL REFERENCES usuario(id),
                           categoria_id      BIGINT         REFERENCES categoria(id),
                           tipo              VARCHAR(10)    NOT NULL,          -- DESPESA | RECEITA
                           valor             NUMERIC(12,2)  NOT NULL CHECK (valor > 0),
                           descricao         VARCHAR(200),
                           ocorrida_em       TIMESTAMPTZ    NOT NULL,
                           mensagem_original TEXT,
                           desfeita_em       TIMESTAMPTZ,                      -- soft delete p/ /desfazer
                           criada_em         TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_transacao_usuario_data ON transacao (usuario_id, ocorrida_em);

CREATE TABLE orcamento (
                           id           BIGSERIAL PRIMARY KEY,
                           usuario_id   BIGINT        NOT NULL REFERENCES usuario(id),
                           categoria_id BIGINT        NOT NULL REFERENCES categoria(id),
                           limite       NUMERIC(12,2) NOT NULL CHECK (limite > 0),
                           UNIQUE (usuario_id, categoria_id)
);

CREATE TABLE regra_categoria (                           -- aprendizado com correções
                                 id            BIGSERIAL PRIMARY KEY,
                                 usuario_id    BIGINT      NOT NULL REFERENCES usuario(id),
                                 palavra_chave VARCHAR(60) NOT NULL,
                                 categoria_id  BIGINT      NOT NULL REFERENCES categoria(id),
                                 UNIQUE (usuario_id, palavra_chave)
);

CREATE TABLE update_processado (                         -- idempotência
                                   update_id     BIGINT PRIMARY KEY,
                                   processado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE alerta_enviado (                            -- evita avisar 80% duas vezes no mês
                                orcamento_id   BIGINT   NOT NULL REFERENCES orcamento(id),
                                mes_referencia DATE     NOT NULL,
                                nivel          SMALLINT NOT NULL,                    -- 80 | 100
                                PRIMARY KEY (orcamento_id, mes_referencia, nivel)
);