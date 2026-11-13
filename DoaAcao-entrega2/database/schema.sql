CREATE TABLE IF NOT EXISTS doacoes (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 doador TEXT NOT NULL CHECK(length(trim(doador)) BETWEEN 1 AND 150),
 destino TEXT NOT NULL CHECK(length(trim(destino)) BETWEEN 1 AND 150),
  status TEXT NOT NULL CHECK(status IN ('RECEBIDA','ENTREGUE'))
);
CREATE TABLE IF NOT EXISTS itens_doacao (
 id INTEGER PRIMARY KEY AUTOINCREMENT,
 doacao_id INTEGER NOT NULL REFERENCES doacoes(id) ON DELETE CASCADE,
 tipo TEXT NOT NULL CHECK(tipo IN ('ALIMENTO','HIGIENE')),
 descricao TEXT NOT NULL CHECK(length(trim(descricao)) BETWEEN 1 AND 150),
 quantidade INTEGER NOT NULL CHECK(quantidade > 0),
 unidade TEXT NOT NULL CHECK(length(trim(unidade)) BETWEEN 1 AND 150),
 validade TEXT,
 CHECK((tipo='ALIMENTO' AND validade IS NOT NULL) OR (tipo='HIGIENE' AND validade IS NULL))
);
CREATE INDEX IF NOT EXISTS idx_itens_doacao ON itens_doacao(doacao_id);
