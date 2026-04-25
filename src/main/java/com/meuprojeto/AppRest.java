package com.meuprojeto;

import com.meuprojeto.dao.CategoriaDAO;
import com.meuprojeto.dao.VendaDAO;
import com.meuprojeto.factory.ConnectionFactory;
import com.meuprojeto.model.Venda;
import io.javalin.Javalin;
import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;
import io.javalin.http.staticfiles.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

public class AppRest {
    public static void main(String[] args) {
        ProdutoDAO dao = new ProdutoDAO();

        var app = Javalin.create(config -> {
            // Define a pasta Web como local dos arquivos estáticos (HTML/CSS/JS)
            config.staticFiles.add("./Web", Location.EXTERNAL);
        }).start(7000);

        // --- 1. ROTA DE LISTAGEM (GET) ---
        // Retorna todos os produtos do MySQL em formato JSON para o site
        app.get("/api/produtos", ctx -> {
            // Busca os produtos da Loja 1 por padrão
            ctx.json(dao.listar(1));
        });

        app.post("/api/produtos", ctx -> {
            Produto p = ctx.bodyAsClass(Produto.class);
            // Se o Front não mandar a loja, assume a Loja 1
            if (p.getIdEmpreendimento() == 0) p.setIdEmpreendimento(1);
            // Se não mandar categoria, assume a Categoria 1 (Geral)
            if (p.getIdCategoria() == 0) p.setIdCategoria(1);

            dao.salvar(p);
            ctx.status(201).result("Cadastrado com sucesso!");
        });

        // --- 3. ROTA DE EXCLUSÃO (DELETE) ---
        // Recebe o ID pela URL (ex: /api/produtos/5) e remove do banco
        app.delete("/api/produtos/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            dao.deletar(id);
            ctx.result("Removido com sucesso");
        });

        // --- 4. ROTA DE ATUALIZAÇÃO (PUT) ---
        // Altera os dados de um produto existente
        app.put("/api/produtos", ctx -> {
            Produto p = ctx.bodyAsClass(Produto.class);
            dao.atualizar(p);
            ctx.result("Atualizado com sucesso");
        });

        // --- 5. ROTA DE VENDA (BAIXA DE ESTOQUE) ---
        // Essa era a função principal do Kotlin: subtrair 1 da quantidade
        app.post("/api/produtos/venda/{id}", ctx -> {
            int idProd = Integer.parseInt(ctx.pathParam("id"));
            int idEmp = 1; // Simulação de empreendimento logado

            // Usamos o método novo que já trata a lógica de banco
            dao.subtrairEstoque(idProd, 1, idEmp);
            ctx.result("Venda realizada!");
        });

        // Rota para LISTAR o histórico de vendas
        app.get("/api/vendas", ctx -> {
            VendaDAO vendaDAO = new VendaDAO();
            ctx.json(vendaDAO.listar());
        });

        // Rota para SALVAR uma nova venda
        app.post("/api/vendas", ctx -> {
            Venda venda = ctx.bodyAsClass(Venda.class);
            VendaDAO vendaDAO = new VendaDAO();
            vendaDAO.salvar(venda);
            ctx.status(201).result("Venda realizada com sucesso!");
        });

        //rota para avisar que um produto foi vendido.
        app.post("/api/produtos/subtrair", ctx -> {
            Map<String, Object> dados = ctx.bodyAsClass(Map.class);

            int id = ((Number) dados.get("id")).intValue();
            int qtd = ((Number) dados.get("quantidadeVendida")).intValue();

            // Pegamos o ID do empreendimento enviado pelo Front-end (Script.js)
            // Se o JS ainda não enviar, coloque um valor padrão para não dar erro:
            int idEmp = dados.containsKey("idEmpreendimento") ?
                    ((Number) dados.get("idEmpreendimento")).intValue() : 1;

            dao.subtrairEstoque(id, qtd, idEmp);
            ctx.status(200).result("Estoque atualizado");
        });

        // Rota para o Front-end preencher o Dropdown de Categorias
        app.get("/api/categorias", ctx -> {
            CategoriaDAO catDao = new CategoriaDAO();
            ctx.json(catDao.listar());
        });

// Rota para listar os Empreendimentos (Lojas) do usuário
        app.post("/api/empreendimentos", ctx -> {
            // Recebe Nome e CNPJ do Front-end
            Map<String, String> dados = ctx.bodyAsClass(Map.class);
            String nome = dados.get("nome");
            String cnpj = dados.get("cnpj");

            // Lógica de salvamento direto (ou via DAO)
            try (Connection conn = ConnectionFactory.criarConexao();
                 PreparedStatement pstm = conn.prepareStatement("INSERT INTO empreendimento (nome, CNPJ) VALUES (?, ?)")) {
                pstm.setString(1, nome);
                pstm.setString(2, cnpj);
                pstm.execute();
                ctx.status(201).result("Empreendimento criado com sucesso!");
            } catch (Exception e) {
                ctx.status(500).result("Erro ao criar: " + e.getMessage());
            }
        });

        System.out.println("✅ SERVIDOR ON: http://localhost:7000/index.html");
    }
}