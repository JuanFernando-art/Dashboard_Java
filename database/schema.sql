create database if not exists estoque_db;

USE estoque_db;

CREATE TABLE produtos (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nome VARCHAR(100) NOT NULL,
                          categoria VARCHAR(50),
                          preco_custo DOUBLE,
                          preco_venda DOUBLE,
                          quantidade INT
);

INSERT INTO produtos (nome, categoria, preco_custo, preco_venda, quantidade) VALUES ('Produto Teste', 10, 50.00, 60.00, 12);

select*from produtos;

