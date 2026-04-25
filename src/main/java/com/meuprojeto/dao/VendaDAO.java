package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.DashboardDTO;
import com.meuprojeto.model.Venda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE: VendaDAO
 * FUNÇÃO: Responsável pelo registro de transações e geração de relatórios financeiros.
 * EXPLICAÇÃO PARA O FRONT-END: Esta classe fornece os dados para os cards coloridos e para
 * as barras de resumo da tela inicial. Ela utiliza cálculos matemáticos direto no SQL
 * para garantir que o lucro líquido e bruto estejam sempre atualizados.
 */
public class VendaDAO {

    /**
     * MÉTODO: salvar
     * OBJETIVO: Registrar uma nova venda finalizada no banco de dados.
     * @param venda Objeto contendo o total da venda e a lista de produtos vendidos (em formato String/JSON).
     */
    public void salvar(Venda venda) {
        String sql = "INSERT INTO venda (total, produtos_vendidos) VALUES (?, ?)";

        // Iniciamos as variáveis de conexão como nulas para controle manual
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setDouble(1, venda.getTotal());
            pstm.setString(2, venda.getProdutosVendidos());

            pstm.execute();
            System.out.println("✅ Venda registrada com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MÉTODO: listar
     * OBJETIVO: Retornar o histórico completo de vendas para ser exibido em tabelas de log.
     * @return Lista de vendas ordenadas da mais recente para a mais antiga.
     */
    public List<Venda> listar() {
        String sql = "SELECT * FROM venda ORDER BY data_venda DESC";
        List<Venda> vendas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rset = pstm.executeQuery()) {

            while (rset.next()) {
                Venda v = new Venda();
                // Mapeia as colunas do banco para o objeto Venda
                v.setId(rset.getInt("id"));
                v.setTotal(rset.getDouble("total"));
                v.setProdutosVendidos(rset.getString("produtos_vendidos"));
                v.setDataVenda(rset.getString("data_venda"));
                vendas.add(v);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vendas;
    }

    /**
     * MÉTODO: buscarResumoPorEmpreendimento / listarResumoEmpreendimentos
     * OBJETIVO: Gerar os dados das barras brancas da Dashboard (Resumo Individual).
     * EXPLICAÇÃO TÉCNICA: O SQL faz um 'LEFT JOIN' para garantir que, mesmo que uma loja
     * não tenha vendas, ela ainda apareça na lista com valores zerados.
     */
    public List<DashboardDTO> listarResumoEmpreendimentos() {
        List<DashboardDTO> lista = new ArrayList<>();

        // SQL COMPLEXO: Soma (SUM) o faturamento e o custo histórico gravado no momento da venda
        String sql = "SELECT e.nome, " +
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
                DashboardDTO d = new DashboardDTO();
                d.setNomeEmpreendimento(rs.getString("nome"));
                d.setLucroBruto(rs.getDouble("bruto"));
                d.setGastoBruto(rs.getDouble("custo"));

                // CÁLCULO DE LUCRO LÍQUIDO: Bruto (Vendas) - Custo (O quanto você pagou no produto)
                d.setLucroLiquido(d.getLucroBruto() - d.getGastoBruto());

                lista.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}