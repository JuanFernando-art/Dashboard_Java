package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// Gerencia as operações de banco de dados para produtos e estoque
public class ProdutoDAO {

    // Registra um novo produto e vincula seu estoque inicial ao empreendimento
    public void salvar(Produto produto) {
        String sqlProduto = "INSERT INTO produto (nome, valor_venda, valor_custo, idCategoria) VALUES (?, ?, ?, ?)";
        String sqlEstoque = "INSERT INTO estoque (quantidadeEstoque, quantidadeInicial, idProduto, idEmpreendimento) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            PreparedStatement pstmP = conn.prepareStatement(sqlProduto, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            if (produto.getIdCategoria() != null) {
                pstmP.setInt(4, produto.getIdCategoria());
            } else {
                pstmP.setNull(4, java.sql.Types.INTEGER);
            }
            pstmP.execute();

            ResultSet rs = pstmP.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);

                PreparedStatement pstmE = conn.prepareStatement(sqlEstoque);
                pstmE.setInt(1, produto.getQuantidade());
                pstmE.setInt(2, produto.getQuantidadeInicial());
                pstmE.setInt(3, idGerado);
                pstmE.setInt(4, produto.getIdEmpreendimento());
                pstmE.execute();
            }
        } catch (Exception e) {
            System.out.println("❌ ERRO NO DAO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Lista produtos de um empreendimento com informações de estoque e categoria
    public List<Produto> listar(int idEmpreendimento) {
        String sql = "SELECT p.idProduto, p.nome, p.valor_venda, p.valor_custo, p.idCategoria, c.nome as categoriaNome, " +
                "e.quantidadeEstoque, e.quantidadeInicial " +
                "FROM produto p " +
                "LEFT JOIN categoria c ON p.idCategoria = c.idCategoria " + 
                "JOIN estoque e ON p.idProduto = e.idProduto " +
                "WHERE e.idEmpreendimento = ?";

        List<Produto> produtos = new ArrayList<>();
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idEmpreendimento);
            ResultSet rset = pstm.executeQuery();

            while (rset.next()) {
                Produto p = new Produto();
                p.setId(rset.getInt("idProduto"));
                p.setNome(rset.getString("nome"));
                p.setPrecoVenda(rset.getDouble("valor_venda"));
                p.setPrecoCusto(rset.getDouble("valor_custo"));
                p.setIdEmpreendimento(idEmpreendimento);
                int idCat = rset.getInt("idCategoria");
                p.setIdCategoria(rset.wasNull() ? null : idCat);
                p.setCategoriaNome(rset.getString("categoriaNome")); 
                p.setQuantidade(rset.getInt("quantidadeEstoque"));
                p.setQuantidadeInicial(rset.getInt("quantidadeInicial"));
                produtos.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return produtos;
    }

    // Atualiza os dados cadastrais do produto e as quantidades em estoque
    public void atualizar(Produto produto) {
        String sqlProd = "UPDATE produto SET nome=?, valor_venda=?, valor_custo=?, idCategoria=? WHERE idProduto=?";
        String sqlEstoque = "UPDATE estoque SET quantidadeEstoque=?, quantidadeInicial=? WHERE idProduto=? AND idEmpreendimento=?";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            PreparedStatement pstmP = conn.prepareStatement(sqlProd);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            if (produto.getIdCategoria() != null) {
                pstmP.setInt(4, produto.getIdCategoria());
            } else {
                pstmP.setNull(4, java.sql.Types.INTEGER);
            }
            pstmP.setInt(5, produto.getId());
            pstmP.executeUpdate();

            PreparedStatement pstmE = conn.prepareStatement(sqlEstoque);
            pstmE.setInt(1, produto.getQuantidade());
            pstmE.setInt(2, produto.getQuantidadeInicial());
            pstmE.setInt(3, produto.getId());
            pstmE.setInt(4, produto.getIdEmpreendimento());
            pstmE.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Deduz a quantidade vendida do estoque garantindo que o saldo não fique negativo
    public void subtrairEstoque(int idProduto, int qtdVendida, int idEmpreendimento) {
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

    // Remove o produto do banco de dados pelo ID
    public void deletar(int id) {
        String sql = "DELETE FROM produto WHERE idProduto = ?";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            pstm.execute();
            System.out.println("✅ Produto removido com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Erro ao deletar: " + e.getMessage());
        }
    }

    // Remove o vínculo de estoque e exclui o produto se ele não possuir outras referências
    public void deletar(int id, int idEmpreendimento) {
        String sqlEstoque = "DELETE FROM estoque WHERE idProduto = ? AND idEmpreendimento = ?";
        String sqlProdutoOrfao = "DELETE FROM produto WHERE idProduto = ? " +
                "AND NOT EXISTS (SELECT 1 FROM estoque WHERE estoque.idProduto = produto.idProduto) " +
                "AND NOT EXISTS (SELECT 1 FROM itemVenda WHERE itemVenda.idProduto = produto.idProduto)";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmEstoque = conn.prepareStatement(sqlEstoque);
                 PreparedStatement pstmProduto = conn.prepareStatement(sqlProdutoOrfao)) {
                pstmEstoque.setInt(1, id);
                pstmEstoque.setInt(2, idEmpreendimento);
                pstmEstoque.executeUpdate();

                pstmProduto.setInt(1, id);
                pstmProduto.executeUpdate();

                conn.commit();
                System.out.println("Produto removido com sucesso!");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Erro ao deletar: " + e.getMessage());
        }
    }
}
