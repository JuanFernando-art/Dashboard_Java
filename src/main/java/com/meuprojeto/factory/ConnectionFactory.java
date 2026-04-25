package com.meuprojeto.factory;

import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class ConnectionFactory {
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Game@9847";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/estoque_db";

    public static Connection criarConexao() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {
        ProdutoDAO dao = new ProdutoDAO();

        // 1. CRIAR
        Produto p1 = new Produto();
        p1.setNome("Mouse Gamer");
        p1.setPrecoCusto(50.00);
        p1.setPrecoVenda(120.00);
        p1.setQuantidade(20);

        // Agora usamos IDs (Certifique-se que esses IDs existem no seu Banco!)
        p1.setIdCategoria(1);      // ID da categoria 'Periféricos' no seu MySQL
        p1.setIdEmpreendimento(1); // ID da 'Oficina' no seu MySQL

        dao.salvar(p1);

        // 2. LISTAR (Agora precisamos passar o ID da loja que queremos ver)
        System.out.println("--- LISTA DA LOJA 1 ---");
        List<Produto> lista = dao.listar(1);

        int idEncontrado = 0;
        for (Produto p : lista) {
            System.out.println("ID: " + p.getId() + " | Nome: " + p.getNome() + " | Qtd: " + p.getQuantidade());
            idEncontrado = p.getId();
        }

        // 3. ATUALIZAR
        if (idEncontrado > 0) {
            Produto pEditado = new Produto();
            pEditado.setId(idEncontrado);
            pEditado.setNome("Mouse Gamer Pro RGB");
            pEditado.setPrecoCusto(55.00);
            pEditado.setPrecoVenda(150.00);
            pEditado.setQuantidade(15);
            pEditado.setIdCategoria(1);
            pEditado.setIdEmpreendimento(1); // Precisamos manter o vínculo da loja

            dao.atualizar(pEditado);
            System.out.println("Produto ID " + idEncontrado + " atualizado!");
        }
    }
}
