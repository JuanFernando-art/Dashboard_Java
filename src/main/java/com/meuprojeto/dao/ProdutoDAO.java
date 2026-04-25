// é o que manda as informações para o banco, é o que faz o banco ler, salvar, listar e atualizar
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
        String sqlProduto = "INSERT INTO produto (nome, valor_venda, valor_custo, idCategoria) VALUES (?, ?, ?, ?)";
        String sqlEstoque = "INSERT INTO estoque (quantidadeEstoque, idProduto, idEmpreendimento) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            // 1. Salva o Produto
            PreparedStatement pstmP = conn.prepareStatement(sqlProduto, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            pstmP.setInt(4, produto.getIdCategoria());
            pstmP.execute();

            // Pega o ID que o MySQL acabou de gerar para o produto
            ResultSet rs = pstmP.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);

                // 2. Salva a Quantidade na tabela de Estoque vinculado à Loja
                PreparedStatement pstmE = conn.prepareStatement(sqlEstoque);
                pstmE.setInt(1, produto.getQuantidade());
                pstmE.setInt(2, idGerado);
                pstmE.setInt(3, produto.getIdEmpreendimento());
                pstmE.execute();
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO NO DAO: " + e.getMessage());
            e.printStackTrace(); // Isso vai mostrar a linha exata do erro no console
        }
    }
    public List<Produto> listar(int idEmpreendimento) {
        // SQL unindo as tabelas para pegar dados físicos e numéricos
        String sql = "SELECT p.idProduto, p.nome, p.valor_venda, p.valor_custo, p.idCategoria, " +
                "e.quantidadeEstoque, e.quantidadeInicial " +
                "FROM produto p " +
                "JOIN estoque e ON p.idProduto = e.idProduto " +
                "WHERE e.idEmpreendimento = ?";

        List<Produto> produtos = new ArrayList<>();
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idEmpreendimento);
            ResultSet rset = pstm.executeQuery();

            while (rset.next()) {
                Produto p = new Produto();
                // MAPEAMENTO: Nome no Java <- Nome no MySQL
                p.setId(rset.getInt("idProduto"));
                p.setNome(rset.getString("nome"));
                p.setPrecoVenda(rset.getDouble("valor_venda"));
                p.setPrecoCusto(rset.getDouble("valor_custo"));
                p.setIdCategoria(rset.getInt("idCategoria"));
                p.setQuantidade(rset.getInt("quantidadeEstoque"));
                p.setQuantidadeInicial(rset.getInt("quantidadeInicial"));
                produtos.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return produtos;
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
        String sqlProd = "UPDATE produto SET nome=?, valor_venda=?, valor_custo=?, idCategoria=? WHERE idProduto=?";
        // AGORA ATUALIZAMOS A quantidadeInicial TAMBÉM!
        String sqlEstoque = "UPDATE estoque SET quantidadeEstoque=?, quantidadeInicial=? WHERE idProduto=? AND idEmpreendimento=?";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            PreparedStatement pstmP = conn.prepareStatement(sqlProd);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            pstmP.setInt(4, produto.getIdCategoria());
            pstmP.setInt(5, produto.getId());
            pstmP.executeUpdate();

            PreparedStatement pstmE = conn.prepareStatement(sqlEstoque);
            pstmE.setInt(1, produto.getQuantidade());
            pstmE.setInt(2, produto.getQuantidadeInicial()); // ESSA LINHA RESOLVE O SEU PROBLEMA
            pstmE.setInt(3, produto.getId());
            pstmE.setInt(4, produto.getIdEmpreendimento());
            pstmE.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public void baixarEstoque(int id, int quantidadeVendida) {
        String sql = "UPDATE produtos SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";
        // O 'AND quantidade >= ?' impede que o estoque fique negativo!
    }
    public void subtrairEstoque(int idProduto, int qtdVendida, int idEmpreendimento) {
        // Agora apontamos para a tabela ESTOQUE e filtramos pelo EMPREENDIMENTO
        String sql = "UPDATE estoque SET quantidadeEstoque = quantidadeEstoque - ? " +
                "WHERE idProduto = ? AND idEmpreendimento = ? AND quantidadeEstoque >= ?";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, qtdVendida);
            pstm.setInt(2, idProduto);
            pstm.setInt(3, idEmpreendimento);
            pstm.setInt(4, qtdVendida);

            pstm.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
