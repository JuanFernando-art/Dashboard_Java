package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void salvar(Categoria categoria) {
        String sql = "INSERT INTO categoria (nome, idCategoriaPai) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstm.setString(1, categoria.getNome());
            if (categoria.getIdCategoriaPai() != null && categoria.getIdCategoriaPai() > 0) {
                pstm.setInt(2, categoria.getIdCategoriaPai());
            } else {
                pstm.setNull(2, Types.INTEGER);
            }
            pstm.execute();
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) categoria.setIdCategoria(rs.getInt(1));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void atualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nome=?, idCategoriaPai=? WHERE idCategoria=?";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, categoria.getNome());
            if (categoria.getIdCategoriaPai() != null && categoria.getIdCategoriaPai() > 0) {
                pstm.setInt(2, categoria.getIdCategoriaPai());
            } else {
                pstm.setNull(2, Types.INTEGER);
            }
            pstm.setInt(3, categoria.getIdCategoria());
            pstm.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<Categoria> listarTodas() {
        String sql = "SELECT idCategoria, nome, idCategoriaPai FROM categoria ORDER BY nome";
        List<Categoria> lista = new ArrayList<>();
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("idCategoria"));
                c.setNome(rs.getString("nome"));
                int pai = rs.getInt("idCategoriaPai");
                c.setIdCategoriaPai(rs.wasNull() ? null : pai);
                lista.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public Categoria buscarPorId(int id) {
        String sql = "SELECT * FROM categoria WHERE idCategoria = ?";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setInt(1, id);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    int pai = rs.getInt("idCategoriaPai");
                    return new Categoria(rs.getInt("idCategoria"), rs.getString("nome"), rs.wasNull() ? null : pai);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public void deletar(int id) {
        String sql = "DELETE FROM categoria WHERE idCategoria = ?";
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) { pstm.setInt(1, id); pstm.execute(); }
        catch (Exception e) { e.printStackTrace(); }
    }
}