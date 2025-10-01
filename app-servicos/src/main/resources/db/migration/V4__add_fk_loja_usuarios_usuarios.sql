-- Adiciona FK para usuarios quando a tabela já existir
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'usuarios')
    THEN
        ALTER TABLE loja_usuarios
        ADD CONSTRAINT fk_loja_usuarios_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;
    END IF;
END$$;
