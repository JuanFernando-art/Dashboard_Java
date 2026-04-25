-- drop database estoque_db;
-- Criação do Banco de Dados
CREATE DATABASE IF NOT EXISTS estoque_db;
USE estoque_db;

-- 1. Tabela DONO: Armazena os usuários do sistema
CREATE TABLE dono (
                      idUsuario INT AUTO_INCREMENT PRIMARY KEY,
                      nome VARCHAR(100) NOT NULL,
                      cpf VARCHAR(16) NOT NULL UNIQUE,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      senha VARCHAR(255) NOT NULL -- Aumentado para suportar criptografia
);

-- 2. Tabela ENDERECO: Armazena localizações de usuários ou empresas
CREATE TABLE endereco (
                          idEndereco INT AUTO_INCREMENT PRIMARY KEY,
                          CEP VARCHAR(10) NOT NULL, -- Mudado para VARCHAR para manter o 0 inicial
                          estado VARCHAR(25) NOT NULL,
                          cidade VARCHAR(30) NOT NULL,
                          bairro VARCHAR(30) NOT NULL,
                          lougradouro VARCHAR(100) NOT NULL, -- Aumentado o tamanho para nomes de rua
                          numero INT NOT NULL
);

-- 3. Tabela CATEGORIA: Organiza os produtos
CREATE TABLE categoria (
                           idCategoria INT PRIMARY KEY AUTO_INCREMENT,
                           nome VARCHAR(50) NOT NULL
);

-- 4. Tabela PRODUTO: Dados principais dos itens
CREATE TABLE produto (
                         idProduto INT PRIMARY KEY AUTO_INCREMENT,
                         nome VARCHAR(50) NOT NULL,
                         valor_venda DOUBLE NOT NULL, -- Alterado de boolean para DOUBLE
                         valor_custo DOUBLE NOT NULL, -- Adicionado para cálculo de lucro
                         idCategoria INT,
                         FOREIGN KEY (idCategoria) REFERENCES categoria(idCategoria)
);
-- 8. Tabela EMPREENDIMENTO: Dados das empresas do usuário
CREATE TABLE empreendimento (
                                idEmpreendimento INT AUTO_INCREMENT PRIMARY KEY,
                                CNPJ VARCHAR(20) NOT NULL UNIQUE,
                                nome VARCHAR(100) NOT NULL,
                                idUsuario INT,
                                idEndereco INT,
                                FOREIGN KEY (idUsuario) REFERENCES dono(idUsuario),
                                FOREIGN KEY (idEndereco) REFERENCES endereco(idEndereco)
);
-- 5. Tabela VENDA: Cabeçalho da venda (Importante para LGPD e Pagamentos)
CREATE TABLE venda (
                       idVenda INT PRIMARY KEY AUTO_INCREMENT,
                       idEmpreendimento INT,
                       dataVenda TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Filtro de 5 anos (LGPD)
                       dataVencimento DATE, -- Para pagamentos programados
                       valorTotal DOUBLE NOT NULL,
                       statusPagamento VARCHAR(20) DEFAULT 'PAGO', -- Ex: PENDENTE, PAGO
                       FOREIGN KEY (idEmpreendimento) REFERENCES empreendimento(idEmpreendimento)
);

-- 6. Tabela ITEMVENDA: Detalhes dos produtos em cada venda
CREATE TABLE itemVenda (
                           idItemVenda INT PRIMARY KEY AUTO_INCREMENT,
                           idVenda INT, -- Relaciona com a tabela venda
                           idProduto INT,
                           quantidadeVenda INT NOT NULL,
                           precoUnitario DOUBLE NOT NULL, -- Valor praticado no momento da venda
                           precoCustoNoMomento DOUBLE NOT NULL, -- Para cálculo preciso de lucro histórico
                           FOREIGN KEY (idVenda) REFERENCES venda(idVenda),
                           FOREIGN KEY (idProduto) REFERENCES produto(idProduto)
);

-- 7. Tabela ESTOQUE: Controle de quantidades por empreendimento
CREATE TABLE estoque (
                         idEstoque INT AUTO_INCREMENT PRIMARY KEY,
                         quantidadeEstoque INT NOT NULL,
                         idProduto INT,
                         idEmpreendimento INT, -- Adicionado para vincular estoque à empresa
                         FOREIGN KEY (idProduto) REFERENCES produto(idProduto),
                         FOREIGN KEY (idEmpreendimento) REFERENCES empreendimento(idEmpreendimento)
);





-- ÍNDICES: Para otimizar a barra de pesquisa (Nome, Categoria, ID)
CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_categoria_nome ON categoria(nome);
CREATE INDEX idx_venda_data ON venda(dataVenda);

INSERT INTO empreendimento (idEmpreendimento, nome, CNPJ) VALUES (1, 'Oficina', '111');
INSERT INTO empreendimento (idEmpreendimento, nome, CNPJ) VALUES (2, 'Padaria', '222');
-- Garante que existe uma categoria e uma loja para o teste não falhar por falta de chave estrangeira
INSERT IGNORE INTO categoria (idCategoria, nome) VALUES (1, 'Geral');
INSERT IGNORE INTO empreendimento (idEmpreendimento, nome, CNPJ) VALUES (1, 'Minha Loja de Teste', '00000000000000');

-- Verificar se a categoria e o empreendimento de teste existem
SELECT * FROM categoria;
SELECT * FROM empreendimento;

-- Verificar se o produto foi salvo (Tabela Produto)
SELECT * FROM produto;

-- Verificar se a quantidade foi para a tabela certa (Tabela Estoque)
SELECT * FROM estoque;

-- Garante que o ID 1 existe para que o Java consiga salvar
INSERT IGNORE INTO categoria (idCategoria, nome) VALUES (1, 'Geral');
INSERT IGNORE INTO dono (idUsuario, nome, cpf, email, senha) VALUES (1, 'pebas', '000', 'pebas@email.com', '123');
INSERT IGNORE INTO empreendimento (idEmpreendimento, nome, CNPJ, idUsuario) VALUES (1, 'Loja Principal', '123', 1);

-- 1. Adicionar a coluna de quantidade inicial/máxima que estava faltando no estoque
ALTER TABLE estoque ADD COLUMN quantidadeInicial INT NOT NULL DEFAULT 0;

-- 2. Corrigir o insert de teste (o seu anterior ia falhar porque falta o idUsuario e idEndereco que são obrigatórios ou precisam ser nulos)
-- Primeiro, vamos permitir que esses campos sejam nulos para teste:
ALTER TABLE empreendimento MODIFY idUsuario INT NULL;
ALTER TABLE empreendimento MODIFY idEndereco INT NULL;

-- 3. Inserir dados básicos para o sistema não travar
INSERT IGNORE INTO categoria (idCategoria, nome) VALUES (1, 'Geral');
INSERT IGNORE INTO empreendimento (idEmpreendimento, nome, CNPJ) VALUES (1, 'Loja Matriz', '000.000/0001-00');

USE estoque_db;

INSERT IGNORE INTO categoria (nome) VALUES ('Geral');
INSERT IGNORE INTO categoria (nome) VALUES ('Periféricos');
INSERT IGNORE INTO categoria (nome) VALUES ('Hardware');

USE estoque_db;

-- Adiciona a coluna de vínculo
ALTER TABLE categoria ADD COLUMN idEmpreendimento INT;

-- Cria a chave estrangeira
ALTER TABLE categoria
    ADD CONSTRAINT fk_categoria_empreendimento
        FOREIGN KEY (idEmpreendimento) REFERENCES empreendimento(idEmpreendimento);

-- Opcional: Se quiser que as categorias atuais pertençam à Loja 1 para não dar erro:
UPDATE categoria SET idEmpreendimento = 1 WHERE idEmpreendimento IS NULL;