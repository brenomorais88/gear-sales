-- V3: criação de lojas e vínculo loja_usuarios (sem FK para usuarios ainda)

CREATE TABLE IF NOT EXISTS lojas (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    endereco TEXT NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(160),
    horario_funcionamento JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_lojas_documento_ativo
ON lojas (documento) WHERE deleted_at IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'loja_role') THEN
        CREATE TYPE loja_role AS ENUM ('ADMIN','VENDEDOR');
    END IF;
END$$;

CREATE TABLE IF NOT EXISTS loja_usuarios (
    id UUID PRIMARY KEY,
    loja_id UUID NOT NULL REFERENCES lojas(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL,                          -- FK será adicionada em migration futura
    role loja_role NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (loja_id, usuario_id)
);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END; $$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_lojas_updated_at ON lojas;
CREATE TRIGGER trg_lojas_updated_at
BEFORE UPDATE ON lojas
FOR EACH ROW EXECUTE PROCEDURE set_updated_at();
