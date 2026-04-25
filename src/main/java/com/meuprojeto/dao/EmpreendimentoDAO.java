package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * CLASSE: EmpreendimentoDAO
 * FUNÇÃO: Responsável por gerenciar a persistência das unidades de negócio (Lojas/Empreendimentos).
 * EXPLICAÇÃO PARA O FRONT-END: Sempre que o usuário clicar no botão "Cadastrar Novo Empreendimento",
 * este código será acionado para criar uma nova identidade no banco de dados. Cada empreendimento
 * gerado aqui terá seu próprio ID único, que será usado para filtrar produtos, vendas e categorias.
 */
public class EmpreendimentoDAO {

    /**
     * MÉTODO: salvar
     * OBJETIVO: Registrar um novo empreendimento no sistema.
     * @param nome O nome fantasia da unidade (ex: "Oficina Central", "Padaria Norte").
     * @param cnpj O número do CNPJ para registro legal no banco de dados.
     */
    public void salvar(String nome, String cnpj) {
        // Comando SQL básico para inserção.
        // Nota: O ID é gerado automaticamente pelo banco (AUTO_INCREMENT).
        String sql = "INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)";

        // O uso do try-with-resources garante que a conexão seja fechada
        // automaticamente, mesmo se ocorrer um erro, evitando travamentos no banco.
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Atribui os valores recebidos do Front-end aos parâmetros '?' do SQL
            pstm.setString(1, nome);
            pstm.setString(2, cnpj);

            // Executa a gravação física no MySQL
            pstm.execute();

        } catch (Exception e) {
            // Imprime o rastro do erro caso a conexão falhe ou o CNPJ seja duplicado (se houver UNIQUE)
            e.printStackTrace();
        }
    }
}