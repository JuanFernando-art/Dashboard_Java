package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.ContaPagamento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE: ContaPagamentoDAO
 * FUNÇÃO: Gerencia a persistência das contas de pagamento (bancárias e PIX).
 */
public class ContaPagamentoDAO {

    public void salvar(ContaPagamento conta) {
        if (verificarDuplicidade(conta)) {
            throw new RuntimeException("Já existe uma conta cadastrada com este Banco, Agência e Número para este empreendimento.");
        }

        String sql = "INSERT INTO contaPagamento (nomeBanco, agencia, conta, tipoConta, pixChave, ativaConta, ativaPix, idEmpreendimento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, conta.getNomeBanco());
            pstm.setString(2, conta.getAgencia());
            pstm.setString(3, conta.getConta());
            pstm.setString(4, conta.getTipoConta());
            pstm.setString(5, conta.getPixChave());
            pstm.setBoolean(6, conta.isAtivaConta());
            pstm.setBoolean(7, conta.isAtivaPix());
            pstm.setInt(8, conta.getIdEmpreendimento());

            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    conta.setIdConta(rs.getInt(1));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO NO DAO (salvar ContaPagamento): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar conta: " + e.getMessage());
        }
    }

    private boolean verificarDuplicidade(ContaPagamento conta) {
        String sql = "SELECT COUNT(*) FROM contaPagamento WHERE nomeBanco = ? AND agencia = ? AND conta = ? AND idEmpreendimento = ? AND idConta != ?";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, conta.getNomeBanco());
            pstm.setString(2, conta.getAgencia());
            pstm.setString(3, conta.getConta());
            pstm.setInt(4, conta.getIdEmpreendimento());
            pstm.setInt(5, conta.getIdConta());
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public List<ContaPagamento> listarPorEmpreendimento(int idEmpreendimento) {
        // Ordena por ativa primeiro e depois por nome do banco
        String sql = "SELECT * FROM contaPagamento WHERE idEmpreendimento = ? ORDER BY ativaConta DESC, ativaPix DESC, nomeBanco ASC";
        List<ContaPagamento> contas = new ArrayList<>();

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idEmpreendimento);
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    ContaPagamento conta = new ContaPagamento();
                    conta.setIdConta(rs.getInt("idConta"));
                    conta.setNomeBanco(rs.getString("nomeBanco"));
                    conta.setAgencia(rs.getString("agencia"));
                    conta.setConta(rs.getString("conta"));
                    conta.setTipoConta(rs.getString("tipoConta"));
                    conta.setPixChave(rs.getString("pixChave"));
                    conta.setAtivaConta(rs.getBoolean("ativaConta"));
                    conta.setAtivaPix(rs.getBoolean("ativaPix"));
                    conta.setIdEmpreendimento(rs.getInt("idEmpreendimento"));
                    contas.add(conta);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ ERRO NO DAO (listarPorEmpreendimento ContaPagamento): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao carregar lista de contas: " + e.getMessage());
        }
        return contas;
    }

    public ContaPagamento buscarPorId(int idConta) {
        String sql = "SELECT * FROM contaPagamento WHERE idConta = ?";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idConta);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    ContaPagamento conta = new ContaPagamento();
                    conta.setIdConta(rs.getInt("idConta"));
                    conta.setNomeBanco(rs.getString("nomeBanco"));
                    conta.setAgencia(rs.getString("agencia"));
                    conta.setConta(rs.getString("conta"));
                    conta.setTipoConta(rs.getString("tipoConta"));
                    conta.setPixChave(rs.getString("pixChave"));
                    conta.setAtivaConta(rs.getBoolean("ativaConta"));
                    conta.setAtivaPix(rs.getBoolean("ativaPix"));
                    conta.setIdEmpreendimento(rs.getInt("idEmpreendimento"));
                    return conta;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ ERRO NO DAO (buscarPorId ContaPagamento): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar detalhes da conta: " + e.getMessage());
        }
        return null;
    }

    public void atualizar(ContaPagamento conta) {
        if (verificarDuplicidade(conta)) {
            throw new RuntimeException("Estes dados bancários já estão em uso em outra conta cadastrada.");
        }

        String sql = "UPDATE contaPagamento SET nomeBanco = ?, agencia = ?, conta = ?, tipoConta = ?, pixChave = ?, ativaConta = ?, ativaPix = ? WHERE idConta = ? AND idEmpreendimento = ?";

        try (Connection conn = ConnectionFactory.criarConexao()) {
            conn.setAutoCommit(false); // Inicia transação para garantir exclusividade
            try {
                // Se o usuário está ativando esta conta para Débito/Crédito, desativa todas as outras
                if (conta.isAtivaConta()) {
                    String sqlDesativar = "UPDATE contaPagamento SET ativaConta = FALSE WHERE idEmpreendimento = ? AND idConta != ?";
                    try (PreparedStatement pstmD = conn.prepareStatement(sqlDesativar)) {
                        pstmD.setInt(1, conta.getIdEmpreendimento());
                        pstmD.setInt(2, conta.getIdConta());
                        pstmD.executeUpdate();
                    }
                }

                // Se o usuário está ativando esta conta para PIX, desativa todas as outras
                if (conta.isAtivaPix()) {
                    String sqlDesativar = "UPDATE contaPagamento SET ativaPix = FALSE WHERE idEmpreendimento = ? AND idConta != ?";
                    try (PreparedStatement pstmD = conn.prepareStatement(sqlDesativar)) {
                        pstmD.setInt(1, conta.getIdEmpreendimento());
                        pstmD.setInt(2, conta.getIdConta());
                        pstmD.executeUpdate();
                    }
                }

                // Atualiza os dados da conta atual
                try (PreparedStatement pstm = conn.prepareStatement(sql)) {
                    pstm.setString(1, conta.getNomeBanco());
                    pstm.setString(2, conta.getAgencia());
                    pstm.setString(3, conta.getConta());
                    pstm.setString(4, conta.getTipoConta());
                    pstm.setString(5, conta.getPixChave());
                    pstm.setBoolean(6, conta.isAtivaConta());
                    pstm.setBoolean(7, conta.isAtivaPix());
                    pstm.setInt(8, conta.getIdConta());
                    pstm.setInt(9, conta.getIdEmpreendimento());
                    pstm.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("❌ ERRO NO DAO (atualizar ContaPagamento): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar conta: " + e.getMessage());
        }
    }

    public void deletar(int idConta, int idEmpreendimento) {
        String sql = "DELETE FROM contaPagamento WHERE idConta = ? AND idEmpreendimento = ?";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idConta);
            pstm.setInt(2, idEmpreendimento);
            pstm.executeUpdate();

        } catch (Exception e) {
            System.err.println("❌ ERRO NO DAO (deletar ContaPagamento): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao excluir conta: " + e.getMessage());
        }
    }

    // Método para buscar a chave PIX de uma conta ativa (pode ser a primeira encontrada ou uma padrão)
    public String buscarPixChaveAtiva(int idEmpreendimento) {
        String sql = "SELECT pixChave FROM contaPagamento WHERE idEmpreendimento = ? AND ativaPix = TRUE AND pixChave IS NOT NULL AND pixChave != '' LIMIT 1";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, idEmpreendimento);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pixChave");
                }
            }
        } catch (Exception e) { /* Ignorar erro, retornar null se não encontrar */ }
        return null;
    }
}