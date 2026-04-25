// é o que manda as informações para o banco, é o que faz o banco ler, salvar, listar e atualizar 
package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Venda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public void salvar(Venda venda) {
        String sql = "INSERT INTO venda (total, produtos_vendidos) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstm = null;

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);
            pstm.setDouble(1, venda.getTotal());
            pstm.setString(2, venda.getProdutosVendidos());
            pstm.execute();
            System.out.println("Venda registrada com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // fechar conexões (mesmo esquema do ProdutoDAO)
        }
    }

    public List<Venda> listar() {
        String sql = "SELECT * FROM venda ORDER BY data_venda DESC";
        List<Venda> vendas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstm = null;
        ResultSet rset = null;

        try {
            conn = ConnectionFactory.criarConexao();
            pstm = conn.prepareStatement(sql);
            rset = pstm.executeQuery();

            while (rset.next()) {
                Venda v = new Venda();
                v.setId(rset.getInt("id"));
                v.setTotal(rset.getDouble("total"));
                v.setProdutosVendidos(rset.getString("produtos_vendidos"));
                v.setDataVenda(rset.getString("data_venda"));
                vendas.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Feche suas conexões aqui como no ProdutoDAO
        }
        return vendas;
    }
}