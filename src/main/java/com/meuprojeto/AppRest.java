package com.meuprojeto;

import io.javalin.Javalin;
import com.meuprojeto.dao.ProdutoDAO;
import io.javalin.http.staticfiles.Location;

public class AppRest {
    public static void main(String[] args) {
        ProdutoDAO dao = new ProdutoDAO();

        // 1. Cria o servidor e aponta para a sua pasta Web (HTML/CSS/JS)
        var app = Javalin.create(config -> {
            // Se você moveu a pasta Web para a raiz, o caminho é "./Web"
            config.staticFiles.add("./Web", Location.EXTERNAL);
        }).start(7000);

        // 2. Rota que o seu JavaScript vai chamar para pegar os produtos
        app.get("/api/produtos", ctx -> {
            ctx.json(dao.listar());
        });

        System.out.println("✅ SERVIDOR ON: http://localhost:7000/index.html");
    }
}