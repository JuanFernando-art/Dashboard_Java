package com.meuprojeto;

import com.meuprojeto.dao.CategoriaDAO;
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
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        UsuarioDAO usuarioDAO = new UsuarioDAO();

        // Inicializa o Javalin e configura a pasta de arquivos web
        var app = Javalin.create(config -> {
            config.staticFiles.add("./Web", Location.EXTERNAL);
        }).start(7000);

        // --- GRUPO: USUARIOS ---

        // Cria um novo usuario e retorna o ID gerado pelo banco para o front-end.
        app.post("/api/usuarios", ctx -> {
            Usuario usuario = ctx.bodyAsClass(Usuario.class);

            if (usuario.getNome() == null || usuario.getNome().isBlank()
                    || usuario.getCpf() == null || usuario.getCpf().isBlank()
                    || usuario.getEmail() == null || usuario.getEmail().isBlank()
                    || usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                ctx.status(400).json(Map.of("erro", "Preencha nome, CPF, email e senha."));
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
            if (p.getIdCategoria() == 0) p.setIdCategoria(1);
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

        // --- 📁 GRUPO: CATEGORIAS ---

        // Lista categorias filtradas pelo ID da loja (Empreendimento)
        app.get("/api/categorias/{idEmp}", ctx -> {
            int idEmp = Integer.parseInt(ctx.pathParam("idEmp"));
            ctx.json(categoriaDAO.listarPorEmpreendimento(idEmp));
        });

        // Cria nova categoria vinculada a uma loja
        app.post("/api/categorias", ctx -> {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String nome = (String) body.get("nome");
            int idEmp = ((Number) body.get("idEmpreendimento")).intValue();
            categoriaDAO.salvar(nome, idEmp);
            ctx.status(201).result("Categoria cadastrada!");
        });

        // --- 🏢 GRUPO: EMPREENDIMENTOS ---

        // Cadastra uma nova unidade de negócio (Loja/Oficina/Padaria)
        app.post("/api/empreendimentos", ctx -> {
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);
            String nome = (String) dados.get("nome");
            String cnpj = (String) dados.get("cnpj");

            if (nome == null || nome.isBlank()
                    || cnpj == null || cnpj.isBlank()
                    || dados.get("idUsuario") == null) {
                ctx.status(400).json(Map.of("erro", "Preencha nome, CNPJ e idUsuario."));
                return;
            }

            int idUsuario = ((Number) dados.get("idUsuario")).intValue();

            try (Connection conn = ConnectionFactory.criarConexao();
                 PreparedStatement pstm = conn.prepareStatement("INSERT INTO empreendimento (nome, CNPJ, idUsuario) VALUES (?, ?, ?)")) {
                pstm.setString(1, nome);
                pstm.setString(2, cnpj);
                pstm.setInt(3, idUsuario);
                pstm.execute();
                ctx.status(201).json(Map.of("mensagem", "Empreendimento criado!"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("erro", "Erro ao criar empreendimento: " + e.getMessage()));
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
