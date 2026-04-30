package com.meuprojeto.dao;

import com.meuprojeto.factory.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * CLASSE: EmpreendimentoDAO
 * FUNÃ‡ÃƒO: ResponsÃ¡vel por gerenciar a persistÃªncia das unidades de negÃ³cio (Lojas/Empreendimentos).
 * EXPLICAÃ‡ÃƒO PARA O FRONT-END: Sempre que o usuÃ¡rio clicar no botÃ£o "Cadastrar Novo Empreendimento",
 * este cÃ³digo serÃ¡ acionado para criar uma nova identidade no banco de dados. Cada empreendimento
 * gerado aqui terÃ¡ seu prÃ³prio ID Ãºnico, que serÃ¡ usado para filtrar produtos e vendas.
 */
public class EmpreendimentoDAO {

    /**
     * MÃ‰TODO: salvar
     * OBJETIVO: Registrar um novo empreendimento no sistema.
     * @param nome O nome fantasia da unidade (ex: "Oficina Central", "Padaria Norte").
     * @param cnpj O nÃºmero do CNPJ para registro legal no banco de dados.
     */
    public void salvar(String nome, String cnpj) {
        // Comando SQL bÃ¡sico para inserÃ§Ã£o.
        // Nota: O ID Ã© gerado automaticamente pelo banco (AUTO_INCREMENT).
        String sql = "INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)";

        // O uso do try-with-resources garante que a conexÃ£o seja fechada
        // automaticamente, mesmo se ocorrer um erro, evitando travamentos no banco.
        try (Connection conn = ConnectionFactory.criarConexao();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Atribui os valores recebidos do Front-end aos parÃ¢metros '?' do SQL
            pstm.setString(1, nome);
            pstm.setString(2, cnpj);

            // Executa a gravaÃ§Ã£o fÃ­sica no MySQL
            pstm.execute();

        } catch (Exception e) {
            // Imprime o rastro do erro caso a conexÃ£o falhe ou o CNPJ seja duplicado (se houver UNIQUE)
            e.printStackTrace();
        }
    }
}
