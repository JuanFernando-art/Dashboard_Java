package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Gerencia a persistência de empreendimentos no banco de dados
public class EmpreendimentoDAO {

    // Insere um novo empreendimento e retorna o ID gerado
    public int salvar(String nome, String cnpj) {
        String sql = "INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, nome);
            pstm.setString(2, cnpj);
            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception e) {
            if (e.getMessage() != null && !e.getMessage().contains("Duplicate entry")) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
