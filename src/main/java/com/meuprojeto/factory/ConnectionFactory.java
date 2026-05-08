package com.meuprojeto.factory;

import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * CLASSE: ConnectionFactory
 * FUNÃ‡ÃƒO: Ã‰ o "Portal de ConexÃ£o". Sem esta classe, o sistema nÃ£o acessa o MySQL.
 * EXPLICAÃ‡ÃƒO PARA O FRONT-END: Esta classe guarda as credenciais do banco de dados
 * e fornece a conexÃ£o ativa para que os DAOs possam ler e gravar informaÃ§Ãµes.
 */
public class ConnectionFactory {

    // Dados de acesso ao seu MySQL Local
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456"; // Sua senha do banco
    // URL: Aponta para o endereÃ§o do banco (localhost) e o nome da base (estoque_db)
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/estoque_db";

    /**
     * MÃ‰TODO: criarConexao
     * OBJETIVO: Tentar abrir uma conexÃ£o com o MySQL usando o Driver JDBC.
     * @return Uma conexÃ£o ativa pronta para ser usada pelos DAOs.
     */
    public static Connection criarConexao() throws Exception {
        // Carrega o Driver do MySQL na memÃ³ria do Java
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Tenta estabelecer a ligaÃ§Ã£o usando as credenciais acima
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    /**
     * MÃ‰TODO MAIN (Sandbox de Testes):
     * OBJETIVO: Este mÃ©todo nÃ£o roda durante o uso do site, ele serve apenas para
     * vocÃª, desenvolvedor, testar as funÃ§Ãµes do ProdutoDAO sem precisar abrir o navegador.
     */
    public static void main(String[] args) {
        ProdutoDAO dao = new ProdutoDAO();

        // --- TESTE 1: CRIAR PRODUTO ---
        Produto p1 = new Produto();
        p1.setNome("Mouse Gamer");
        p1.setPrecoCusto(50.00);
        p1.setPrecoVenda(120.00);
        p1.setQuantidade(20);
        p1.setIdEmpreendimento(1);

        dao.salvar(p1);
        System.out.println("âœ… Teste de Cadastro finalizado.");

        // --- TESTE 2: LISTAR PRODUTOS ---
        // Passamos o ID 1 para simular a visualizaÃ§Ã£o da Loja 1
        System.out.println("--- LISTA DA LOJA 1 ---");
        List<Produto> lista = dao.listar(1);

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
