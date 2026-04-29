package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLASSE: CategoriaDAO
 * FUNÇÃO: Gerencia todas as operações de banco de dados relacionadas às categorias.
 * EXPLICAÇÃO: Esta classe permite que cada empreendimento (loja) tenha
 * seu próprio conjunto de categorias. Isso evita que, por exemplo, categorias de uma
 * "Oficina" apareçam em uma "Padaria".
 */
public class CategoriaDAO {
    private static final String[] CATEGORIAS_PADRAO = {
            "Geral",
            "Alimentos",
            "Bebidas",
            "Limpeza",
            "Eletronicos",
            "Servicos",
            "Perifericos",
            "Hardware"
    };

    /**
     * MÉTODO: listarPorEmpreendimento
     * OBJETIVO: Buscar no banco apenas as categorias vinculadas a um ID de empreendimento específico.
     * @param idEmp O ID da loja que o usuário selecionou na Dashboard.
     * @return Uma lista de objetos Categoria (ID e Nome) para preencher os menus do site.
     */
    public List<Categoria> listarPorEmpreendimento(int idEmp) {
        garantirCategoriasPadrao(idEmp);

        // Comando SQL com '?' para evitar ataques de SQL Injection
        String sql = "SELECT * FROM categoria WHERE idEmpreendimento = ?";
        List<Categoria> lista = new ArrayList<>();

        // Abre conexão com o banco e prepara o comando automaticamente (try-with-resources)
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Substitui o '?' pelo ID da loja recebido como parâmetro
            pstm.setInt(1, idEmp);

            // Executa a consulta e armazena os resultados na variável rs
            ResultSet rs = pstm.executeQuery();

            // Percorre cada linha encontrada no banco de dados
            while (rs.next()) {
                Categoria c = new Categoria();
                // Mapeia a coluna do MySQL para o objeto Java
                c.setId(rs.getInt("idCategoria"));
                c.setNome(rs.getString("nome"));
                lista.add(c);
            }
        } catch (Exception e) {
            // Registra erros de conexão ou sintaxe SQL no console
            e.printStackTrace();
        }
        return lista;
    }

    private void garantirCategoriasPadrao(int idEmp) {
        String sqlContar = "SELECT COUNT(*) FROM categoria WHERE idEmpreendimento = ?";
        String sqlInserir = "INSERT INTO categoria (nome, idEmpreendimento) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement contar = conn.prepareStatement(sqlContar)) {

            contar.setInt(1, idEmp);

            try (ResultSet rs = contar.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return;
                }
            }

            try (PreparedStatement inserir = conn.prepareStatement(sqlInserir)) {
                for (String nome : CATEGORIAS_PADRAO) {
                    inserir.setString(1, nome);
                    inserir.setInt(2, idEmp);
                    inserir.addBatch();
                }
                inserir.executeBatch();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MÉTODO: salvar
     * OBJETIVO: Criar uma nova categoria associada a uma loja específica.
     * @param nome O nome da categoria (ex: "Peças", "Alimentos").
     * @param idEmp O ID do empreendimento dono desta categoria.
     */
    public void salvar(String nome, int idEmp) {
        // Insere o nome e o vínculo com o empreendimento
        String sql = "INSERT INTO categoria (nome, idEmpreendimento) VALUES (?, ?)";

        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Define os valores que serão inseridos nas colunas
            pstm.setString(1, nome);
            pstm.setInt(2, idEmp);

            // Executa o comando de inserção no banco
            pstm.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
