package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Usuario;
import com.meuprojeto.security.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsuarioDAO {

    public int salvar(Usuario usuario) throws Exception {
        String sql = "INSERT INTO dono (nome, email, senha, cpf_encrypted, cpf_hash) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, usuario.getNome());
            pstm.setString(2, usuario.getEmail());
            pstm.setString(3, SecurityUtil.hashPassword(usuario.getSenha()));
            pstm.setString(4, SecurityUtil.encryptCpf(usuario.getCpf()));
            pstm.setString(5, SecurityUtil.hashCpf(usuario.getCpf()));
            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("cpf_encrypted")) {
                return salvarSchemaAntigo(usuario);
            }

            throw e;
        }

        throw new Exception("Nao foi possivel recuperar o ID do usuario criado.");
    }

    private int salvarSchemaAntigo(Usuario usuario) throws Exception {
        String sql = "INSERT INTO dono (nome, cpf, email, senha) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstm.setString(1, usuario.getNome());
            pstm.setString(2, usuario.getCpf());
            pstm.setString(3, usuario.getEmail());
            pstm.setString(4, SecurityUtil.hashPassword(usuario.getSenha()));
            pstm.executeUpdate();

            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new Exception("Nao foi possivel recuperar o ID do usuario criado.");
    }

    public Usuario autenticar(String email, String senha) throws Exception {
        String sql = "SELECT idUsuario, nome, email, senha FROM dono WHERE email = ?";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, email);

            try (ResultSet rs = pstm.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                if (!SecurityUtil.verifyPassword(senha, rs.getString("senha"))) {
                    return null;
                }

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("idUsuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                return usuario;
            }
        }
    }

    public boolean senhaConfere(int idUsuario, String senha) throws Exception {
        String sql = "SELECT senha FROM dono WHERE idUsuario = ?";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idUsuario);

            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next() && SecurityUtil.verifyPassword(senha, rs.getString("senha"));
            }
        }
    }
}
