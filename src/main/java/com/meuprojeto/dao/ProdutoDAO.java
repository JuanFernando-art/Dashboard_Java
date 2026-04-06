package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    public void salvar(Produto produto) {
        String sql = "INSERT INTO produtos (nome, categoria, preco_custo, preco_venda, quantidade, quantidade_inicial) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);

            pstm.setString(1, produto.getNome());
            pstm.setString(2, produto.getCategoria());
            pstm.setDouble(3, produto.getPrecoCusto());
            pstm.setDouble(4, produto.getPrecoVenda());
            pstm.setInt(5, produto.getQuantidade());
            pstm.setInt(6, produto.getQuantidadeInicial());
            pstm.execute();
            System.out.println("Produto salvo com sucesso no MySQL!");

        } catch (Exception e) {
            System.out.println("Erro ao salvar produto: " + e.getMessage());
        } finally {
            try {
                if (pstm != null) pstm.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public List<Produto> listar() {
        String sql = "SELECT * FROM produtos"; // A ordem SQL
        List<Produto> produtos = new ArrayList<>(); // A lista que vai guardar os resultados

        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rset = null; // Onde os dados do MySQL "pousam" antes de ir pro Java

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);

            // Executa a busca e guarda no rset
            rset = pstm.executeQuery();

            // Enquanto houver uma próxima linha no banco...
            while (rset.next()) {
                Produto p = new Produto();
                p.setId(rset.getInt("id"));
                p.setNome(rset.getString("nome"));
                p.setCategoria(rset.getString("categoria"));
                p.setQuantidade(rset.getInt("quantidade"));
                p.setQuantidadeInicial(rset.getInt("quantidade_inicial"));
                p.setPrecoVenda(rset.getDouble("preco_venda"));
                p.setPrecoCusto(rset.getDouble("preco_custo"));
                produtos.add(p);
            }
        } catch (Exception e) {
            System.out.println("Erro ao listar produtos: " + e.getMessage());
        } finally {
            // Agora sim, a parte do fechamento que você já tinha:
            try {
                if (rset != null) rset.close();
                if (pstm != null) pstm.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return produtos; // Devolve a lista cheia para o seu método main
    }

    public void deletar(int id) {
        String sql = "DELETE FROM produtos WHERE id = ?";

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);
            pstm.setInt(1, id);

            pstm.execute();
            System.out.println("Produto removido com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao deletar: " + e.getMessage());
        } finally {
            // ... fechar conexões como nos outros métodos ...
        }
    }

    public void atualizar(Produto produto) {
        String sql = "UPDATE produtos SET nome=?, categoria=?, preco_custo=?, preco_venda=?, quantidade=?, quantidade_inicial=? WHERE id=?";

        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);

            pstm.setString(1, produto.getNome());
            pstm.setString(2, produto.getCategoria());
            pstm.setDouble(3, produto.getPrecoCusto());
            pstm.setDouble(4, produto.getPrecoVenda());
            pstm.setInt(5, produto.getQuantidade());
            pstm.setInt(6, produto.getQuantidadeInicial());
            pstm.setInt(7, produto.getId());

            int linhasAfetadas = pstm.executeUpdate(); // Use executeUpdate para saber o resultado

            if (linhasAfetadas > 0) {
                System.out.println("Produto atualizado com sucesso!");
            } else {
                System.out.println("Atenção: Nenhum produto encontrado com o ID informado.");
            }
                System.out.println("Produto atualizado com sucesso!");
            } catch (Exception e) {
                System.out.println("Erro ao atualizar: " + e.getMessage());
            } finally {
                // ... fechar conexões ...
        }
    }
}
