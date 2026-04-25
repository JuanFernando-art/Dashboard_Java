package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    public List<Categoria> listar() {
        String sql = "SELECT * FROM categoria";
        List<Categoria> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("idCategoria"));
                c.setNome(rs.getString("nome"));
                lista.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }
}