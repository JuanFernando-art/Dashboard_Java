-- drop database estoque_db;

CREATE DATABASE IF NOT EXISTS estoque_db;
USE estoque_db;

-- 1. Tabela DONO: Armazena os usuÃ¡rios do sistema
CREATE TABLE dono (
                      idUsuario INT AUTO_INCREMENT PRIMARY KEY,
                      nome VARCHAR(100) NOT NULL,
                      cpf VARCHAR(16) NOT NULL UNIQUE,
                      email VARCHAR(100) NOT NULL UNIQUE,
                      senha VARCHAR(255) NOT NULL -- Aumentado para suportar criptografia
);

-- 2. Tabela ENDERECO: Armazena localizaÃ§Ãµes de usuÃ¡rios ou empresas
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
                           nome VARCHAR(50) NOT NULL UNIQUE
);

-- 4. Tabela PRODUTO: Dados principais dos itens
CREATE TABLE produto (
                         idProduto INT PRIMARY KEY AUTO_INCREMENT,
                         nome VARCHAR(50) NOT NULL,
                         valor_venda DOUBLE NOT NULL, -- Alterado de boolean para DOUBLE
                         valor_custo DOUBLE NOT NULL, -- Adicionado para cÃ¡lculo de lucro
                         idCategoria INT,
                         FOREIGN KEY (idCategoria) REFERENCES categoria(idCategoria)
);
-- 8. Tabela EMPREENDIMENTO: Dados das empresas do usuÃ¡rio
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
                           precoCustoNoMomento DOUBLE NOT NULL, -- Para cÃ¡lculo preciso de lucro histÃ³rico
                           FOREIGN KEY (idVenda) REFERENCES venda(idVenda),
                           FOREIGN KEY (idProduto) REFERENCES produto(idProduto)
);

-- 7. Tabela ESTOQUE: Controle de quantidades por empreendimento
CREATE TABLE estoque (
                         idEstoque INT AUTO_INCREMENT PRIMARY KEY,
                         quantidadeEstoque INT NOT NULL,
                         idProduto INT,
                         idEmpreendimento INT, -- Adicionado para vincular estoque Ã  empresa
                         FOREIGN KEY (idProduto) REFERENCES produto(idProduto),
                         FOREIGN KEY (idEmpreendimento) REFERENCES empreendimento(idEmpreendimento)
);

-- INDEX: Para otimizar a barra de pesquisa (Nome, Categoria, ID)
CREATE INDEX idx_produto_nome ON produto(nome);
CREATE INDEX idx_categoria_nome ON categoria(nome);
CREATE INDEX idx_venda_data ON venda(dataVenda);

-- 1. Adicionar a coluna de quantidade inicial/mÃ¡xima que estava faltando no estoque
ALTER TABLE estoque ADD COLUMN quantidadeInicial INT NOT NULL DEFAULT 0;

-- 2. Corrigir o insert de teste (o seu anterior ia falhar porque falta o idUsuario e idEndereco que sÃ£o obrigatÃ³rios ou precisam ser nulos)
-- Primeiro, vamos permitir que esses campos sejam nulos para teste:
ALTER TABLE empreendimento MODIFY idUsuario INT NULL;
ALTER TABLE empreendimento MODIFY idEndereco INT NULL;

