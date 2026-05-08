USE estoque_db;

ALTER TABLE dono MODIFY cpf VARCHAR(16) NULL;
ALTER TABLE dono ADD COLUMN cpf_encrypted TEXT NULL;
ALTER TABLE dono ADD COLUMN cpf_hash VARCHAR(64) NULL;
ALTER TABLE dono ADD UNIQUE INDEX idx_dono_cpf_hash (cpf_hash);

-- Depois de ajustar ou recadastrar usuarios antigos, execute:
-- ALTER TABLE dono MODIFY cpf_encrypted TEXT NOT NULL;
-- ALTER TABLE dono MODIFY cpf_hash VARCHAR(64) NOT NULL;
