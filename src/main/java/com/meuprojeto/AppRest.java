package com.meuprojeto;

import com.meuprojeto.dao.VendaDAO;
import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.dao.UsuarioDAO;
import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.DashboardDTO;
import com.meuprojeto.model.Venda;
import com.meuprojeto.model.Produto;
import com.meuprojeto.model.Usuario;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * CLASSE: AppRest
 * FUNÇÃO: Servidor Web (API) que conecta o Front-end ao Back-end.
 * EXPLICAÇÃO PARA O FRONT-END: Este arquivo define todas as rotas (URLs) do sistema.
 * O servidor roda por padrão na porta 7000. As pastas de arquivos estáticos (HTML/CSS/JS)
 * devem estar dentro da pasta "./Web".
 */
public class AppRest {
    public static void main(String[] args) {

        // Instancia os DAOs para gerenciar o banco de dados
        ProdutoDAO produtoDAO = new ProdutoDAO();
        VendaDAO vendaDAO = new VendaDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // Inicializa o Javalin e configura a pasta de arquivos web
        var app = Javalin.create(config -> {
            config.staticFiles.add("./Web", Location.EXTERNAL);
        }).start(7000);

        app.exception(Exception.class, (e, ctx) -> {
            if (ctx.status().getCode() < 400) {
                ctx.status(500);
            }
            ctx.json(Map.of("erro", e.getMessage() == null ? "Erro interno no servidor." : e.getMessage()));
        });

        // --- GRUPO: USUARIOS ---

        // Cria um novo usuario e retorna o ID gerado pelo banco para o front-end.
        app.post("/api/usuarios", ctx -> {
            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            String cpfNormalizado = usuario.getCpf() == null ? "" : usuario.getCpf().replaceAll("\\D", "");

            if (usuario.getNome() == null || usuario.getNome().isBlank()
                    || usuario.getCpf() == null || usuario.getCpf().isBlank()
                    || usuario.getEmail() == null || usuario.getEmail().isBlank()
                    || usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                ctx.status(400).json(Map.of("erro", "Preencha nome, CPF, email e senha."));
                return;
            }

            if (cpfNormalizado.length() != 11) {
                ctx.status(400).json(Map.of("erro", "Informe um CPF com 11 digitos."));
                return;
            }

            if (usuario.getSenha().length() < 8) {
                ctx.status(400).json(Map.of("erro", "A senha precisa ter pelo menos 8 caracteres."));
                return;
            }

            try {
                int idUsuario = usuarioDAO.salvar(usuario);
                ctx.status(201).json(Map.of(
                        "idUsuario", idUsuario,
                        "nome", usuario.getNome(),
                        "email", usuario.getEmail()
                ));
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    ctx.status(409).json(Map.of("erro", "CPF ou email ja cadastrado."));
                    return;
                }

                ctx.status(500).json(Map.of("erro", "Erro ao cadastrar usuario: " + e.getMessage()));
            }
        });

        // --- 📦 GRUPO: PRODUTOS ---

        // Lista produtos (Padrão: Loja 1)
        app.post("/api/login", ctx -> {
            Usuario credenciais = ctx.bodyAsClass(Usuario.class);

            if (credenciais.getEmail() == null || credenciais.getEmail().isBlank()
                    || credenciais.getSenha() == null || credenciais.getSenha().isBlank()) {
                ctx.status(400).json(Map.of("erro", "Preencha email e senha."));
                return;
            }

            try {
                Usuario usuario = usuarioDAO.autenticar(credenciais.getEmail(), credenciais.getSenha());

                if (usuario == null) {
                    ctx.status(401).json(Map.of("erro", "Email ou senha invalidos."));
                    return;
                }

                ctx.json(Map.of(
                        "idUsuario", usuario.getIdUsuario(),
                        "nome", usuario.getNome(),
                        "email", usuario.getEmail()
                ));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao fazer login: " + e.getMessage()));
            }
        });

        app.get("/api/produtos", ctx -> {
            String idEmpreendimento = ctx.queryParam("idEmpreendimento");
            int idEmp = idEmpreendimento != null && !idEmpreendimento.isBlank()
                    ? Integer.parseInt(idEmpreendimento)
                    : 1;
            ctx.json(produtoDAO.listar(idEmp));
        });

        // Cadastra novo produto
        app.post("/api/produtos", ctx -> {
            Produto p = ctx.bodyAsClass(Produto.class);
            // Garante que o produto tenha um vínculo inicial caso o front esqueça de enviar
            if (p.getIdEmpreendimento() == 0) p.setIdEmpreendimento(1);
            produtoDAO.salvar(p);
            ctx.status(201).result("Cadastrado com sucesso!");
        });

        // Atualiza dados de um produto existente
        app.put("/api/produtos", ctx -> {
            Produto p = ctx.bodyAsClass(Produto.class);
            produtoDAO.atualizar(p);
            ctx.result("Atualizado com sucesso");
        });

        // Remove um produto pelo ID passado na URL
        app.delete("/api/produtos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            produtoDAO.deletar(id);
            ctx.result("Removido com sucesso");
        });

        // Baixa manual de estoque (usado no momento da venda ou perda)
        app.post("/api/produtos/subtrair", ctx -> {
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);
            int id = ((Number) dados.get("id")).intValue();
            int qtd = ((Number) dados.get("quantidadeVendida")).intValue();
            int idEmp = dados.containsKey("idEmpreendimento") ? ((Number) dados.get("idEmpreendimento")).intValue() : 1;
            produtoDAO.subtrairEstoque(id, qtd, idEmp);
            ctx.status(200).result("Estoque atualizado");
        });

        // --- 💰 GRUPO: VENDAS ---

        // Retorna todo o histórico de vendas
        app.get("/api/vendas", ctx -> {
            String idEmpreendimento = ctx.queryParam("idEmpreendimento");
            if (idEmpreendimento != null && !idEmpreendimento.isBlank()) {
                ctx.json(vendaDAO.listarPorEmpreendimento(Integer.parseInt(idEmpreendimento)));
                return;
            }

            ctx.json(vendaDAO.listar());
        });

        // Registra uma nova venda concluída
        app.post("/api/vendas", ctx -> {
            try {
                Venda venda = ctx.bodyAsClass(Venda.class);
                vendaDAO.salvar(venda);
                ctx.status(201).json(Map.of("mensagem", "Venda realizada com sucesso!"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao registrar venda: " + e.getMessage()));
            }
        });

        // --- 🏢 GRUPO: EMPREENDIMENTOS ---

        app.get("/api/empreendimentos/{id}", ctx -> {
            int idEmpreendimento = Integer.parseInt(ctx.pathParam("id"));

            String sql = "SELECT e.idEmpreendimento, e.nome, e.CNPJ, en.CEP, en.estado, en.cidade, en.bairro, en.lougradouro, en.numero " +
                    "FROM empreendimento e " +
                    "LEFT JOIN endereco en ON e.idEndereco = en.idEndereco " +
                    "WHERE e.idEmpreendimento = ?";

            try (Connection conn = ConnectionFactory.criarConexao();
                 PreparedStatement pstm = conn.prepareStatement(sql)) {

                pstm.setInt(1, idEmpreendimento);

                try (ResultSet rs = pstm.executeQuery()) {
                    if (!rs.next()) {
                        ctx.status(404).json(Map.of("erro", "Empreendimento nao encontrado."));
                        return;
                    }

                    String cep = rs.getString("CEP");
                    String estado = rs.getString("estado");
                    String cidade = rs.getString("cidade");
                    String bairro = rs.getString("bairro");
                    String logradouro = rs.getString("lougradouro");
                    String cnpj = rs.getString("CNPJ");

                    ctx.json(Map.of(
                            "idEmpreendimento", rs.getInt("idEmpreendimento"),
                            "nome", rs.getString("nome"),
                            "cnpj", cnpj == null ? "" : cnpj,
                            "endereco", Map.of(
                                    "cep", cep == null ? "" : cep,
                                    "estado", estado == null ? "" : estado,
                                    "cidade", cidade == null ? "" : cidade,
                                    "bairro", bairro == null ? "" : bairro,
                                    "logradouro", logradouro == null ? "" : logradouro,
                                    "numero", rs.getInt("numero")
                            )
                    ));
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao buscar empreendimento: " + e.getMessage()));
            }
        });

        // Cadastra uma nova unidade de negócio (Loja/Oficina/Padaria)
        app.post("/api/empreendimentos", ctx -> {
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);
            String nome = (String) dados.get("nome");
            String cnpj = (String) dados.get("cnpj");
            Map<String, Object> endereco = (Map<String, Object>) dados.get("endereco");

            if (nome == null || nome.isBlank()
                    || cnpj == null || cnpj.isBlank()
                    || dados.get("idUsuario") == null
                    || endereco == null) {
                ctx.status(400).json(Map.of("erro", "Preencha nome, CNPJ, idUsuario e endereco."));
                return;
            }

            int idUsuario = ((Number) dados.get("idUsuario")).intValue();
            String cep = (String) endereco.get("cep");
            String estado = (String) endereco.get("estado");
            String cidade = (String) endereco.get("cidade");
            String bairro = (String) endereco.get("bairro");
            String logradouro = (String) endereco.get("logradouro");
            Object numeroRaw = endereco.get("numero");

            if (cep == null || cep.isBlank()
                    || estado == null || estado.isBlank()
                    || cidade == null || cidade.isBlank()
                    || bairro == null || bairro.isBlank()
                    || logradouro == null || logradouro.isBlank()
                    || numeroRaw == null) {
                ctx.status(400).json(Map.of("erro", "Preencha todos os campos do endereco."));
                return;
            }

            int numero = ((Number) numeroRaw).intValue();

            try (Connection conn = ConnectionFactory.criarConexao()) {
                conn.setAutoCommit(false);

                try {
                    int idEndereco;
                    try (PreparedStatement pstmEndereco = conn.prepareStatement(
                            "INSERT INTO endereco (CEP, estado, cidade, bairro, lougradouro, numero) VALUES (?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        pstmEndereco.setString(1, cep);
                        pstmEndereco.setString(2, estado);
                        pstmEndereco.setString(3, cidade);
                        pstmEndereco.setString(4, bairro);
                        pstmEndereco.setString(5, logradouro);
                        pstmEndereco.setInt(6, numero);
                        pstmEndereco.executeUpdate();

                        try (ResultSet rs = pstmEndereco.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new Exception("Nao foi possivel recuperar o ID do endereco.");
                            }
                            idEndereco = rs.getInt(1);
                        }
                    }

                    try (PreparedStatement pstm = conn.prepareStatement(
                            "INSERT INTO empreendimento (nome, CNPJ, idUsuario, idEndereco) VALUES (?, ?, ?, ?)")) {
                        pstm.setString(1, nome);
                        pstm.setString(2, cnpj);
                        pstm.setInt(3, idUsuario);
                        pstm.setInt(4, idEndereco);
                        pstm.executeUpdate();
                    }

                    conn.commit();
                    ctx.status(201).json(Map.of("mensagem", "Empreendimento criado!"));
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao criar empreendimento: " + e.getMessage()));
            }
        });

        app.put("/api/empreendimentos/{id}", ctx -> {
            int idEmpreendimento = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);
            String nome = (String) dados.get("nome");
            Map<String, Object> endereco = (Map<String, Object>) dados.get("endereco");

            if (nome == null || nome.isBlank() || endereco == null) {
                ctx.status(400).json(Map.of("erro", "Preencha todos os campos editaveis."));
                return;
            }

            String cep = (String) endereco.get("cep");
            String estado = (String) endereco.get("estado");
            String cidade = (String) endereco.get("cidade");
            String bairro = (String) endereco.get("bairro");
            String logradouro = (String) endereco.get("logradouro");
            Object numeroRaw = endereco.get("numero");

            if (cep == null || cep.isBlank()
                    || estado == null || estado.isBlank()
                    || cidade == null || cidade.isBlank()
                    || bairro == null || bairro.isBlank()
                    || logradouro == null || logradouro.isBlank()
                    || numeroRaw == null) {
                ctx.status(400).json(Map.of("erro", "Preencha todos os campos editaveis."));
                return;
            }

            int numero = ((Number) numeroRaw).intValue();

            try (Connection conn = ConnectionFactory.criarConexao()) {
                conn.setAutoCommit(false);

                try {
                    Integer idEndereco = null;

                    try (PreparedStatement busca = conn.prepareStatement(
                            "SELECT idEndereco FROM empreendimento WHERE idEmpreendimento = ?")) {
                        busca.setInt(1, idEmpreendimento);

                        try (ResultSet rs = busca.executeQuery()) {
                            if (!rs.next()) {
                                ctx.status(404).json(Map.of("erro", "Empreendimento nao encontrado."));
                                conn.rollback();
                                return;
                            }

                            int enderecoAtual = rs.getInt("idEndereco");
                            if (!rs.wasNull()) {
                                idEndereco = enderecoAtual;
                            }
                        }
                    }

                    if (idEndereco == null) {
                        try (PreparedStatement criaEndereco = conn.prepareStatement(
                                "INSERT INTO endereco (CEP, estado, cidade, bairro, lougradouro, numero) VALUES (?, ?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            criaEndereco.setString(1, cep);
                            criaEndereco.setString(2, estado);
                            criaEndereco.setString(3, cidade);
                            criaEndereco.setString(4, bairro);
                            criaEndereco.setString(5, logradouro);
                            criaEndereco.setInt(6, numero);
                            criaEndereco.executeUpdate();

                            try (ResultSet rs = criaEndereco.getGeneratedKeys()) {
                                if (!rs.next()) {
                                    throw new Exception("Nao foi possivel recuperar o ID do endereco.");
                                }
                                idEndereco = rs.getInt(1);
                            }
                        }

                        try (PreparedStatement vinculaEndereco = conn.prepareStatement(
                                "UPDATE empreendimento SET idEndereco = ? WHERE idEmpreendimento = ?")) {
                            vinculaEndereco.setInt(1, idEndereco);
                            vinculaEndereco.setInt(2, idEmpreendimento);
                            vinculaEndereco.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement atualizaEndereco = conn.prepareStatement(
                                "UPDATE endereco SET CEP = ?, estado = ?, cidade = ?, bairro = ?, lougradouro = ?, numero = ? WHERE idEndereco = ?")) {
                            atualizaEndereco.setString(1, cep);
                            atualizaEndereco.setString(2, estado);
                            atualizaEndereco.setString(3, cidade);
                            atualizaEndereco.setString(4, bairro);
                            atualizaEndereco.setString(5, logradouro);
                            atualizaEndereco.setInt(6, numero);
                            atualizaEndereco.setInt(7, idEndereco);
                            atualizaEndereco.executeUpdate();
                        }
                    }

                    try (PreparedStatement atualizaEmpreendimento = conn.prepareStatement(
                            "UPDATE empreendimento SET nome = ? WHERE idEmpreendimento = ?")) {
                        atualizaEmpreendimento.setString(1, nome);
                        atualizaEmpreendimento.setInt(2, idEmpreendimento);
                        atualizaEmpreendimento.executeUpdate();
                    }

                    conn.commit();
                    ctx.json(Map.of("mensagem", "Empreendimento atualizado!"));
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao atualizar empreendimento: " + e.getMessage()));
            }
        });

        app.delete("/api/empreendimentos/{id}", ctx -> {
            int idEmpreendimento = Integer.parseInt(ctx.pathParam("id"));
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);

            if (dados.get("idUsuario") == null || dados.get("senha") == null
                    || String.valueOf(dados.get("senha")).isBlank()) {
                ctx.status(400).json(Map.of("erro", "Informe a senha para excluir o empreendimento."));
                return;
            }

            int idUsuario = ((Number) dados.get("idUsuario")).intValue();
            String senha = String.valueOf(dados.get("senha"));

            try (Connection conn = ConnectionFactory.criarConexao()) {
                conn.setAutoCommit(false);

                try {
                    if (!usuarioDAO.senhaConfere(idUsuario, senha)) {
                        ctx.status(401).json(Map.of("erro", "Senha incorreta."));
                        conn.rollback();
                        return;
                    }

                    Integer idEndereco = null;
                    int donoEmpreendimento;
                    try (PreparedStatement buscaEmpreendimento = conn.prepareStatement(
                            "SELECT idUsuario, idEndereco FROM empreendimento WHERE idEmpreendimento = ?")) {
                        buscaEmpreendimento.setInt(1, idEmpreendimento);

                        try (ResultSet rs = buscaEmpreendimento.executeQuery()) {
                            if (!rs.next()) {
                                ctx.status(404).json(Map.of("erro", "Empreendimento nao encontrado."));
                                conn.rollback();
                                return;
                            }

                            donoEmpreendimento = rs.getInt("idUsuario");
                            int enderecoAtual = rs.getInt("idEndereco");
                            if (!rs.wasNull()) {
                                idEndereco = enderecoAtual;
                            }
                        }
                    }

                    if (donoEmpreendimento != idUsuario) {
                        ctx.status(403).json(Map.of("erro", "Este empreendimento nao pertence ao usuario logado."));
                        conn.rollback();
                        return;
                    }

                    try (PreparedStatement buscaVendas = conn.prepareStatement(
                            "SELECT COUNT(*) AS total FROM venda WHERE idEmpreendimento = ?")) {
                        buscaVendas.setInt(1, idEmpreendimento);

                        try (ResultSet rs = buscaVendas.executeQuery()) {
                            rs.next();
                            if (rs.getInt("total") > 0) {
                                ctx.status(409).json(Map.of("erro", "Nao e possivel excluir um empreendimento com vendas registradas."));
                                conn.rollback();
                                return;
                            }
                        }
                    }

                    try (PreparedStatement buscaEstoque = conn.prepareStatement(
                            "SELECT COUNT(*) AS total FROM estoque WHERE idEmpreendimento = ? AND quantidadeEstoque > 0")) {
                        buscaEstoque.setInt(1, idEmpreendimento);

                        try (ResultSet rs = buscaEstoque.executeQuery()) {
                            rs.next();
                            if (rs.getInt("total") > 0) {
                                ctx.status(409).json(Map.of("erro", "Zere o estoque antes de excluir o empreendimento."));
                                conn.rollback();
                                return;
                            }
                        }
                    }

                    java.util.List<Integer> produtosDoEmpreendimento = new java.util.ArrayList<>();
                    try (PreparedStatement buscaProdutos = conn.prepareStatement(
                            "SELECT idProduto FROM estoque WHERE idEmpreendimento = ?")) {
                        buscaProdutos.setInt(1, idEmpreendimento);

                        try (ResultSet rs = buscaProdutos.executeQuery()) {
                            while (rs.next()) {
                                produtosDoEmpreendimento.add(rs.getInt("idProduto"));
                            }
                        }
                    }

                    try (PreparedStatement removeEstoque = conn.prepareStatement(
                            "DELETE FROM estoque WHERE idEmpreendimento = ?")) {
                        removeEstoque.setInt(1, idEmpreendimento);
                        removeEstoque.executeUpdate();
                    }

                    try (PreparedStatement removeProdutoOrfao = conn.prepareStatement(
                            "DELETE FROM produto WHERE idProduto = ? " +
                                    "AND NOT EXISTS (SELECT 1 FROM estoque WHERE estoque.idProduto = produto.idProduto) " +
                                    "AND NOT EXISTS (SELECT 1 FROM itemVenda WHERE itemVenda.idProduto = produto.idProduto)")) {
                        for (Integer idProduto : produtosDoEmpreendimento) {
                            removeProdutoOrfao.setInt(1, idProduto);
                            removeProdutoOrfao.addBatch();
                        }
                        removeProdutoOrfao.executeBatch();
                    }

                    try (PreparedStatement removeEmpreendimento = conn.prepareStatement(
                            "DELETE FROM empreendimento WHERE idEmpreendimento = ?")) {
                        removeEmpreendimento.setInt(1, idEmpreendimento);
                        removeEmpreendimento.executeUpdate();
                    }

                    if (idEndereco != null) {
                        try (PreparedStatement removeEndereco = conn.prepareStatement(
                                "DELETE FROM endereco WHERE idEndereco = ? " +
                                        "AND NOT EXISTS (SELECT 1 FROM empreendimento WHERE empreendimento.idEndereco = endereco.idEndereco)")) {
                            removeEndereco.setInt(1, idEndereco);
                            removeEndereco.executeUpdate();
                        }
                    }

                    conn.commit();
                    ctx.json(Map.of("mensagem", "Empreendimento excluido!"));
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao excluir empreendimento: " + e.getMessage()));
            }
        });

        // --- 📊 GRUPO: DASHBOARD ---

        // Retorna os dados das BARRAS BRANCAS (Resumo por loja)
        app.get("/api/dashboard/empreendimentos", ctx -> {
            String idUsuario = ctx.queryParam("idUsuario");
            if (idUsuario != null && !idUsuario.isBlank()) {
                ctx.json(vendaDAO.listarResumoEmpreendimentosPorUsuario(Integer.parseInt(idUsuario)));
                return;
            }

            ctx.json(vendaDAO.listarResumoEmpreendimentos());
        });

        // Retorna os dados dos CARDS COLORIDOS (Soma total de todas as lojas)
        app.get("/api/dashboard/total", ctx -> {
            String idUsuario = ctx.queryParam("idUsuario");
            List<DashboardDTO> lista = idUsuario != null && !idUsuario.isBlank()
                    ? vendaDAO.listarResumoEmpreendimentosPorUsuario(Integer.parseInt(idUsuario))
                    : vendaDAO.listarResumoEmpreendimentos();
            double totalBruto = lista.stream().mapToDouble(DashboardDTO::getLucroBruto).sum();
            double totalGasto = lista.stream().mapToDouble(DashboardDTO::getGastoBruto).sum();
            ctx.json(Map.of(
                    "totalBruto", totalBruto,
                    "totalGasto", totalGasto,
                    "totalLiquido", totalBruto - totalGasto
            ));
        });

        System.out.println("SERVIDOR ON: http://localhost:7000/index.html");
    }
}
