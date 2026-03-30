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
select*from produtos;

