-- create database estoque_db;

USE estoque_db;

CREATE TABLE produtos (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nome VARCHAR(100) NOT NULL,
                          categoria VARCHAR(50),
                          preco_custo DOUBLE,
                          preco_venda DOUBLE,
                          quantidade INT
);

CREATE TABLE vendas (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        total DOUBLE NOT NULL,
                        produtos_vendidos TEXT -- Aqui guardaremos a lista de nomes/quantidades
);

ALTER TABLE produtos ADD COLUMN quantidade_inicial INT DEFAULT 100;
UPDATE produtos SET quantidade_inicial = quantidade WHERE quantidade_inicial IS NULL;
ALTER TABLE produtos MODIFY COLUMN quantidade_inicial INT NOT NULL;

-- 1. Desativa temporariamente o modo de segurança
SET SQL_SAFE_UPDATES = 0;
-- 2. Atualiza a capacidade máxima para ser igual à quantidade atual para todos os produtos
UPDATE produtos SET quantidade_inicial = quantidade;
-- 3. Reativa o modo de segurança (boa prática)
SET SQL_SAFE_UPDATES = 1;

select*from produtos;

