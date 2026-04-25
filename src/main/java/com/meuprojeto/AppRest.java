package com.meuprojeto;

import com.meuprojeto.dao.CategoriaDAO;
import com.meuprojeto.dao.VendaDAO;
import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.DashboardDTO;
import com.meuprojeto.model.Venda;
import com.meuprojeto.model.Produto;
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

        // Inicializa o Javalin e configura a pasta de arquivos web
        var app = Javalin.create(config -> {
            config.staticFiles.add("./Web", Location.EXTERNAL);
        }).start(7000);

        // --- 📦 GRUPO: PRODUTOS ---

        // Lista produtos (Padrão: Loja 1)
        app.get("/api/produtos", ctx -> {
            ctx.json(produtoDAO.listar(1));
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
            ctx.json(vendaDAO.listar());
        });

        // Registra uma nova venda concluída
        app.post("/api/vendas", ctx -> {
            Venda venda = ctx.bodyAsClass(Venda.class);
            vendaDAO.salvar(venda);
            ctx.status(201).result("Venda realizada com sucesso!");
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
            Map<String, String> dados = ctx.bodyAsClass(Map.class);
            String nome = dados.get("nome");
            String cnpj = dados.get("cnpj");
            try (Connection conn = ConnectionFactory.criarConexao();
                 PreparedStatement pstm = conn.prepareStatement("INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)")) {
                pstm.setString(1, nome);
                pstm.setString(2, cnpj);
                pstm.execute();
                ctx.status(201).result("Empreendimento criado!");
            } catch (Exception e) {
                ctx.status(500).result("Erro: " + e.getMessage());
            }
        });

        // --- 📊 GRUPO: DASHBOARD ---

        // Retorna os dados das BARRAS BRANCAS (Resumo por loja)
        app.get("/api/dashboard/empreendimentos", ctx -> {
            ctx.json(vendaDAO.listarResumoEmpreendimentos());
        });

        // Retorna os dados dos CARDS COLORIDOS (Soma total de todas as lojas)
        app.get("/api/dashboard/total", ctx -> {
            List<DashboardDTO> lista = vendaDAO.listarResumoEmpreendimentos();
            double totalBruto = lista.stream().mapToDouble(DashboardDTO::getLucroBruto).sum();
            double totalGasto = lista.stream().mapToDouble(DashboardDTO::getGastoBruto).sum();
            ctx.json(Map.of(
                    "totalBruto", totalBruto,
                    "totalGasto", totalGasto,
                    "totalLiquido", totalBruto - totalGasto
            ));
        });

        System.out.println("✅ SERVIDOR ON: http://localhost:7000/index.html");
    }
}