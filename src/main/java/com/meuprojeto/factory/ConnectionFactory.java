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

        // 1. CRIAR (Para garantir que temos algo no banco)
        Produto p1 = new Produto();
        p1.setNome("Mouse Gamer");
        p1.setCategoria("Periféricos");
        p1.setPrecoCusto(50.00);
        p1.setPrecoVenda(120.00);
        p1.setQuantidade(20);
        dao.salvar(p1);

        // 2. LISTAR (Para descobrir qual ID o MySQL deu para ele)
        System.out.println("--- LISTA APÓS CADASTRO ---");
        List<Produto> lista = dao.listar();
        int idEncontrado = 0;

        for (Produto p : lista) {
            System.out.println("ID: " + p.getId() + " | Nome: " + p.getNome());
            idEncontrado = p.getId(); // Pegamos o último ID gerado
        }

        // 3. ATUALIZAR (Usando o ID que acabamos de descobrir)
        if (idEncontrado > 0) {
            Produto pEditado = new Produto();
            pEditado.setId(idEncontrado);
            pEditado.setNome("Mouse Gamer Pro RGB"); // Nome novo
            pEditado.setCategoria("Periféricos");
            pEditado.setPrecoCusto(55.00);
            pEditado.setPrecoVenda(150.00);
            pEditado.setQuantidade(15);

            dao.atualizar(pEditado);
            System.out.println("Produto ID " + idEncontrado + " atualizado!");
        }

        // 4. LISTAR NOVAMENTE (Para ver a mudança)
        System.out.println("\n--- LISTA FINAL ATUALIZADA ---");
        for (Produto p : dao.listar()) {
            System.out.println("Nome: " + p.getNome() + " | Venda: R$ " + p.getPrecoVenda());
        }

        // OPCIONAL: Se quiser deixar o banco limpo no final, descomente a linha abaixo:
        // dao.deletar(idEncontrado);
    }

}
