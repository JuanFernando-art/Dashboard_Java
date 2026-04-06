package com.meuprojeto;

import io.javalin.Javalin;
import com.meuprojeto.dao.ProdutoDAO;
import com.meuprojeto.model.Produto;
import io.javalin.http.staticfiles.Location;

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
            ctx.json(dao.listar());
        });

        // --- 2. ROTA DE CADASTRO (POST) ---
        // Recebe os dados do formulário do site e salva no banco
        app.post("/api/produtos", ctx -> {
            Produto p = ctx.bodyAsClass(Produto.class); // Transforma o JSON em Objeto Java
            dao.salvar(p);
            ctx.status(201).result("Produto cadastrado com sucesso!");
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
            int id = Integer.parseInt(ctx.pathParam("id"));

            // Lógica: Buscamos a lista, achamos o produto pelo ID e reduzimos 1
            // (Em um sistema maior, faríamos uma busca direta por ID no DAO)
            for (Produto p : dao.listar()) {
                if (p.getId() == id) {
                    if (p.getQuantidade() > 0) {
                        p.setQuantidade(p.getQuantidade() - 1);
                        dao.atualizar(p); // Salva a nova quantidade no banco
                        ctx.result("Venda realizada!");
                    } else {
                        ctx.status(400).result("Erro: Estoque zerado!");
                    }
                    break;
                }
            }
        });

        System.out.println("✅ SERVIDOR ON: http://localhost:7000/index.html");
    }
}