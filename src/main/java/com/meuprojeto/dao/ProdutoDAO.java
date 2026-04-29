package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE: ProdutoDAO
 * FUNÇÃO: É o cérebro que gerencia o estoque físico e os dados comerciais dos produtos.
 * EXPLICAÇÃO PARA O FRONT-END: Diferente de um sistema simples, aqui o produto existe em uma tabela 
 * e a quantidade existe em outra (estoque). Isso permite que o mesmo produto tenha quantidades 
 * diferentes em lojas diferentes.
 */
public class ProdutoDAO {

    /**
     * MÉTODO: salvar
     * OBJETIVO: Realiza um salvamento duplo. Primeiro cria o produto e depois vincula a 
     * quantidade inicial ao empreendimento logado.
     */
    public void salvar(Produto produto) {
        // SQL para as duas tabelas distintas
        String sqlProduto = "INSERT INTO produto (nome, valor_venda, valor_custo, idCategoria) VALUES (?, ?, ?, ?)";
        String sqlEstoque = "INSERT INTO estoque (quantidadeEstoque, quantidadeInicial, idProduto, idEmpreendimento) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            // 1. SALVAMENTO DO PRODUTO: 
            // RETURN_GENERATED_KEYS avisa o Java que queremos saber qual ID o MySQL criou para esse produto.
            PreparedStatement pstmP = conn.prepareStatement(sqlProduto, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            pstmP.setInt(4, produto.getIdCategoria());
            pstmP.execute();

            // Pega o ID (chave primária) que o MySQL acabou de gerar
            ResultSet rs = pstmP.getGeneratedKeys();
            if (rs.next()) {
                int idGerado = rs.getInt(1);

                // 2. SALVAMENTO NO ESTOQUE:
                // Vincula o produto recém-criado à quantidade informada e ao empreendimento atual.
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

    /**
     * MÉTODO: listar
     * OBJETIVO: Retornar a lista de produtos completa, unindo dados de nome/preço com a quantidade.
     * @param idEmpreendimento Filtra para trazer apenas os itens da loja que o usuário está visualizando.
     */
    public List<Produto> listar(int idEmpreendimento) {
        // O comando JOIN une a tabela 'produto' com a 'estoque' usando o ID em comum.
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
                // MAPEAMENTO: Extrai os dados do banco e coloca dentro do objeto Java (Model)
                p.setId(rset.getInt("idProduto"));
                p.setNome(rset.getString("nome"));
                p.setPrecoVenda(rset.getDouble("valor_venda"));
                p.setPrecoCusto(rset.getDouble("valor_custo"));
                p.setIdCategoria(rset.getInt("idCategoria"));
                p.setIdEmpreendimento(idEmpreendimento);
                p.setQuantidade(rset.getInt("quantidadeEstoque"));
                p.setQuantidadeInicial(rset.getInt("quantidadeInicial"));
                produtos.add(p);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return produtos;
    }

    /**
     * MÉTODO: atualizar
     * OBJETIVO: Atualiza tanto as informações básicas quanto as quantidades no estoque.
     */
    public void atualizar(Produto produto) {
        // Atualiza a "ficha" do produto
        String sqlProd = "UPDATE produto SET nome=?, valor_venda=?, valor_custo=?, idCategoria=? WHERE idProduto=?";
        // Atualiza a "quantidade" na loja específica
        String sqlEstoque = "UPDATE estoque SET quantidadeEstoque=?, quantidadeInicial=? WHERE idProduto=? AND idEmpreendimento=?";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            // Executa atualização dos dados básicos
            PreparedStatement pstmP = conn.prepareStatement(sqlProd);
            pstmP.setString(1, produto.getNome());
            pstmP.setDouble(2, produto.getPrecoVenda());
            pstmP.setDouble(3, produto.getPrecoCusto());
            pstmP.setInt(4, produto.getIdCategoria());
            pstmP.setInt(5, produto.getId());
            pstmP.executeUpdate();

            // Executa atualização das quantidades (incluindo a inicial para correção de relatórios)
            PreparedStatement pstmE = conn.prepareStatement(sqlEstoque);
            pstmE.setInt(1, produto.getQuantidade());
            pstmE.setInt(2, produto.getQuantidadeInicial());
            pstmE.setInt(3, produto.getId());
            pstmE.setInt(4, produto.getIdEmpreendimento());
            pstmE.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * MÉTODO: subtrairEstoque
     * OBJETIVO: Diminuir a quantidade do produto quando uma venda é realizada.
     * SEGURANÇA: O 'AND quantidadeEstoque >= ?' evita que o estoque fique negativo.
     */
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

    /**
     * MÉTODO: deletar
     * OBJETIVO: Remover o produto do sistema usando o ID único.
     */
    public void deletar(int id) {
        // Atenção: No banco real, as chaves estrangeiras devem estar configuradas
        // para deletar o estoque automaticamente quando o produto for removido.
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
}
