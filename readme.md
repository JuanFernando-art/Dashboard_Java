# Gestor ERP

Sistema ERP desenvolvido para gerenciamento completo de pequenas e médias operações comerciais, centralizando o controle financeiro, estoque, produtos e vendas em uma única plataforma.

## Funcionalidades

- 📦 Cadastro e gerenciamento de produtos
- 🏷️ Categorias personalizadas para organização dos produtos
- 📊 Controle de estoque em tempo real
- 💰 Registro e acompanhamento de vendas
- 📈 indicadores de desempenho
- ⚠️ Alertas de estoque baixo
- 💵 Controle financeiro de entradas e saídas
- 🧾 Histórico detalhado de movimentações
- 🔍 Pesquisa e filtros
- 📑 Relatórios gerenciais
- 🔄 Atualização automática de saldo em estoque após vendas
- 🔐 Validação de dados e regras de negócio
- 🌐 Interface web responsiva

## Objetivo

O Gestor ERP foi desenvolvido com o objetivo de simplificar a administração empresarial, permitindo que gestores acompanhem o desempenho financeiro, controlem o estoque e realizem o gerenciamento de produtos e vendas de forma prática, organizada e eficiente.

# Dashboard Java

## Pré-requisitos

Antes de iniciar, certifique-se de ter instalado em sua máquina:

- Java JDK 17 ou superior
- Apache Maven
- MySQL Server 8.0
- Git

## Instalação

Clone o repositório:

```bash
git clone <URL_DO_REPOSITORIO>
```

Acesse a pasta do projeto:

```bash
cd Dashboard_Java
```

## Configuração do Ambiente

Antes de executar a aplicação, crie um arquivo `.env.local` na raiz do projeto com o seguinte conteúdo:

```env
APP_ENV=dev
DB_URL=jdbc:mysql://localhost:3306/estoque_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USER=root
DB_PASSWORD=
```

> Caso utilize um usuário ou senha diferente no MySQL, ajuste os valores de `DB_USER` e `DB_PASSWORD` conforme necessário.

## Configuração do Banco de Dados

Com o MySQL Server em execução, execute o script de criação do banco:

```bash
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root < database\schema.sql
```

> Caso seu usuário MySQL possua senha, utilize:
>
> ```bash
> "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p < database\schema.sql
> ```

## Executando a Aplicação

Compile e inicie o servidor:

```bash
mvn clean compile exec:java -Dexec.mainClass=com.meuprojeto.AppRest
```

## Acesso

Após a inicialização, a aplicação estará disponível em:

```text
http://localhost:7000/index.html
```

## Tecnologias Utilizadas

- Java 17+
- Maven
- MySQL
- JDBC
- Javalin
- HTML, CSS e JavaScript
