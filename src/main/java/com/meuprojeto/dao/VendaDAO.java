package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.DashboardDTO;
import com.meuprojeto.model.ItemVenda;
import com.meuprojeto.model.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VendaDAO {

    public void salvar(Venda venda) {
        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new IllegalArgumentException("A venda precisa ter pelo menos um item.");
        }

        String sqlVenda = "INSERT INTO venda (idEmpreendimento, valorTotal) VALUES (?, ?)";
        String sqlItem = "INSERT INTO itemVenda (idVenda, idProduto, quantidadeVenda, precoUnitario, precoCustoNoMomento) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {
                pstmVenda.setInt(1, venda.getIdEmpreendimento());
                pstmVenda.setDouble(2, venda.getTotal());
                pstmVenda.executeUpdate();

                int idVenda;
                try (ResultSet rs = pstmVenda.getGeneratedKeys()) {
                    if (!rs.next()) {
                        throw new Exception("Nao foi possivel recuperar o ID da venda.");
                    }
                    idVenda = rs.getInt(1);
                }

                try (PreparedStatement pstmItem = conn.prepareStatement(sqlItem)) {
                    for (ItemVenda item : venda.getItens()) {
                        pstmItem.setInt(1, idVenda);
                        pstmItem.setInt(2, item.getIdProduto());
                        pstmItem.setInt(3, item.getQuantidade());
                        pstmItem.setDouble(4, item.getPrecoUnitario());
                        pstmItem.setDouble(5, item.getPrecoCustoNoMomento());
                        pstmItem.addBatch();
                    }
                    pstmItem.executeBatch();
                }

                conn.commit();
                System.out.println("Venda registrada com sucesso!");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<Venda> listar() {
        String sql = "SELECT v.idVenda, v.valorTotal, v.dataVenda, " +
                "IFNULL(GROUP_CONCAT(CONCAT(p.nome, ' (', iv.quantidadeVenda, ')') SEPARATOR ', '), '') as produtosVendidos " +
                "FROM venda v " +
                "LEFT JOIN itemVenda iv ON v.idVenda = iv.idVenda " +
                "LEFT JOIN produto p ON iv.idProduto = p.idProduto " +
                "GROUP BY v.idVenda, v.valorTotal, v.dataVenda " +
                "ORDER BY v.dataVenda DESC";

        return listarComSql(sql, null);
    }

    public List<Venda> listarPorEmpreendimento(int idEmpreendimento) {
        String sql = "SELECT v.idVenda, v.valorTotal, v.dataVenda, " +
                "IFNULL(GROUP_CONCAT(CONCAT(p.nome, ' (', iv.quantidadeVenda, ')') SEPARATOR ', '), '') as produtosVendidos " +
                "FROM venda v " +
                "LEFT JOIN itemVenda iv ON v.idVenda = iv.idVenda " +
                "LEFT JOIN produto p ON iv.idProduto = p.idProduto " +
                "WHERE v.idEmpreendimento = ? " +
                "GROUP BY v.idVenda, v.valorTotal, v.dataVenda " +
                "ORDER BY v.dataVenda DESC";

        return listarComSql(sql, idEmpreendimento);
    }

    private List<Venda> listarComSql(String sql, Integer idEmpreendimento) {
        List<Venda> vendas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            if (idEmpreendimento != null) {
                pstm.setInt(1, idEmpreendimento);
            }

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Venda venda = new Venda();
                    venda.setId(rs.getInt("idVenda"));
                    venda.setTotal(rs.getDouble("valorTotal"));
                    venda.setProdutosVendidos(rs.getString("produtosVendidos"));
                    venda.setDataVenda(rs.getString("dataVenda"));
                    vendas.add(venda);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vendas;
    }

    public List<DashboardDTO> listarResumoEmpreendimentos() {
        List<DashboardDTO> lista = new ArrayList<>();
        String sql = "SELECT e.idEmpreendimento, e.nome, " +
                "IFNULL(SUM(iv.precoUnitario * iv.quantidadeVenda), 0) as bruto, " +
                "IFNULL(SUM(iv.precoCustoNoMomento * iv.quantidadeVenda), 0) as custo " +
                "FROM empreendimento e " +
                "LEFT JOIN venda v ON e.idEmpreendimento = v.idEmpreendimento " +
                "LEFT JOIN itemVenda iv ON v.idVenda = iv.idVenda " +
                "GROUP BY e.idEmpreendimento, e.nome";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                lista.add(mapDashboard(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<DashboardDTO> listarResumoEmpreendimentosPorUsuario(int idUsuario) {
        List<DashboardDTO> lista = new ArrayList<>();
        String sql = "SELECT e.idEmpreendimento, e.nome, " +
                "IFNULL(SUM(iv.precoUnitario * iv.quantidadeVenda), 0) as bruto, " +
                "IFNULL(SUM(iv.precoCustoNoMomento * iv.quantidadeVenda), 0) as custo " +
                "FROM empreendimento e " +
                "LEFT JOIN venda v ON e.idEmpreendimento = v.idEmpreendimento " +
                "LEFT JOIN itemVenda iv ON v.idVenda = iv.idVenda " +
                "WHERE e.idUsuario = ? " +
                "GROUP BY e.idEmpreendimento, e.nome";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idUsuario);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapDashboard(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private DashboardDTO mapDashboard(ResultSet rs) throws Exception {
        DashboardDTO d = new DashboardDTO();
        d.setIdEmpreendimento(rs.getInt("idEmpreendimento"));
        d.setNomeEmpreendimento(rs.getString("nome"));
        d.setLucroBruto(rs.getDouble("bruto"));
        d.setGastoBruto(rs.getDouble("custo"));
        d.setLucroLiquido(d.getLucroBruto() - d.getGastoBruto());
        return d;
    }
}
