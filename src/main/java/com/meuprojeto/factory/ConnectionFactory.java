package com.meuprojeto.factory;

import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * CLASSE: ConnectionFactory
 * FUNÇÃO: É o "Portal de Conexão". Sem esta classe, o sistema não acessa o MySQL.
 * EXPLICAÇÃO PARA O FRONT-END: Esta classe guarda as credenciais do banco de dados
 * e fornece a conexão ativa para que os DAOs possam ler e gravar informações.
 */
public class ConnectionFactory {

    // Dados de acesso ao seu MySQL Local
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; // Sua senha do banco
    // URL: Aponta para o endereço do banco (localhost) e o nome da base (estoque_db)
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/estoque_db";

    /**
     * MÉTODO: criarConexao
     * OBJETIVO: Tentar abrir uma conexão com o MySQL usando o Driver JDBC.
     * @return Uma conexão ativa pronta para ser usada pelos DAOs.
     */
    public static Connection criarConexao() throws Exception {
        // Carrega o Driver do MySQL na memória do Java
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Tenta estabelecer a ligação usando as credenciais acima
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    /**
     * MÉTODO MAIN (Sandbox de Testes):
     * OBJETIVO: Este método não roda durante o uso do site, ele serve apenas para
     * você, desenvolvedor, testar as funções do ProdutoDAO sem precisar abrir o navegador.
     */
    public static void main(String[] args) {
        ProdutoDAO dao = new ProdutoDAO();

        // --- TESTE 1: CRIAR PRODUTO ---
        Produto p1 = new Produto();
        p1.setNome("Mouse Gamer");
        p1.setPrecoCusto(50.00);
        p1.setPrecoVenda(120.00);
        p1.setQuantidade(20);

        // ATENÇÃO: Estes IDs devem existir nas tabelas 'categoria' e 'empreendimento' do seu MySQL
        p1.setIdCategoria(1);
        p1.setIdEmpreendimento(1);

        dao.salvar(p1);
        System.out.println("✅ Teste de Cadastro finalizado.");

        // --- TESTE 2: LISTAR PRODUTOS ---
        // Passamos o ID 1 para simular a visualização da Loja 1
        System.out.println("--- LISTA DA LOJA 1 ---");
        List<Produto> lista = dao.listar(1);

        int idEncontrado = 0;
        for (Produto p : lista) {
            System.out.println("ID: " + p.getId() + " | Nome: " + p.getNome() + " | Qtd: " + p.getQuantidade());
            idEncontrado = p.getId();
        }

        // --- TESTE 3: ATUALIZAR PRODUTO ---
        // Se a lista não estiver vazia, pegamos o último ID para testar a edição
        if (idEncontrado > 0) {
            Produto pEditado = new Produto();
            pEditado.setId(idEncontrado);
            pEditado.setNome("Mouse Gamer Pro RGB");
            pEditado.setPrecoCusto(55.00);
            pEditado.setPrecoVenda(150.00);
            pEditado.setQuantidade(15);
            pEditado.setIdCategoria(1);
            pEditado.setIdEmpreendimento(1);

            dao.atualizar(pEditado);
            System.out.println("✅ Produto ID " + idEncontrado + " atualizado com sucesso!");
        }
    }
}