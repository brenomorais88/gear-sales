CREATE TABLE IF NOT EXISTS categoriasEstabelecimento (
  id   SERIAL PRIMARY KEY,
  nome TEXT NOT NULL UNIQUE
);