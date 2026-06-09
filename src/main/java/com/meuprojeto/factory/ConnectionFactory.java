package com.meuprojeto.factory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import com.meuprojeto.config.AppConfig;
import com.meuprojeto.dao.EmpreendimentoDAO;
import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;

//Esta classe guarda as credenciais do banco de dados 
public class ConnectionFactory {

    private static final String DEV_DATABASE_URL = "jdbc:mysql://localhost:3306/estoque_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEV_DATABASE_USER = "root";
    private static final String DEV_DATABASE_PASSWORD = "";

        public static Connection criarConexao() throws Exception {
    
        Class.forName("com.mysql.cj.jdbc.Driver");

        String databaseUrl = AppConfig.envOrDevFallback("DB_URL", DEV_DATABASE_URL);
        String username = AppConfig.envOrDevFallback("DB_USER", DEV_DATABASE_USER);
        String password = AppConfig.envOrDevFallback("DB_PASSWORD", DEV_DATABASE_PASSWORD);

        System.out.println("Tentando conectar ao banco com usuario: " + username + " na URL: " + databaseUrl);

        return DriverManager.getConnection(databaseUrl, username, password);
    }

    /**
     * MÃ‰TODO MAIN (Sandbox de Testes):
     * OBJETIVO: Este mÃ©todo nÃ£o roda durante o uso do site, ele serve apenas para
     * vocÃª, desenvolvedor, testar as funÃ§Ãµes do ProdutoDAO sem precisar abrir o navegador.
     */
    public static void main(String[] args) {
        // --- PASSO ZERO: CRIAR UM EMPREENDIMENTO ---
        // Como o banco usa chaves estrangeiras, o ID 1 precisa existir.
        EmpreendimentoDAO empDao = new EmpreendimentoDAO();
        int idEmp = empDao.salvar("Loja Matriz Teste", "12.345.678/0001-99");

        ProdutoDAO dao = new ProdutoDAO();

        // --- TESTE 1: CRIAR PRODUTO ---
        Produto p1 = new Produto();
        p1.setNome("Mouse Gamer");
        p1.setPrecoCusto(50.00);
        p1.setPrecoVenda(120.00);
        p1.setQuantidade(20);
        p1.setIdEmpreendimento(idEmp > 0 ? idEmp : 1);

        dao.salvar(p1);
        System.out.println("âœ… Teste de Cadastro finalizado.");

        // --- TESTE 2: LISTAR PRODUTOS ---
        // Passamos o ID 1 para simular a visualizaÃ§Ã£o da Loja 1
        System.out.println("--- LISTA DA LOJA " + (idEmp > 0 ? idEmp : 1) + " ---");
        List<Produto> lista = dao.listar(idEmp > 0 ? idEmp : 1);

        int idEncontrado = 0;
        for (Produto p : lista) {
            System.out.println("ID: " + p.getId() + " | Nome: " + p.getNome() + " | Qtd: " + p.getQuantidade());
            idEncontrado = p.getId();
        }

        // --- TESTE 3: ATUALIZAR PRODUTO ---
        // Se a lista nÃ£o estiver vazia, pegamos o Ãºltimo ID para testar a ediÃ§Ã£o
        if (idEncontrado > 0) {
            Produto pEditado = new Produto();
            pEditado.setId(idEncontrado);
            pEditado.setNome("Mouse Gamer Pro RGB");
            pEditado.setPrecoCusto(55.00);
            pEditado.setPrecoVenda(150.00);
            pEditado.setQuantidade(15);
            pEditado.setIdEmpreendimento(1);

            dao.atualizar(pEditado);
            System.out.println("âœ… Produto ID " + idEncontrado + " atualizado com sucesso!");
        }
    }
}
