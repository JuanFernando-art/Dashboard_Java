package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EmpreendimentoDAO {

    public void salvar(String nome, String cnpj) {
        String sql = "INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, nome);
            pstm.setString(2, cnpj);
            pstm.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
