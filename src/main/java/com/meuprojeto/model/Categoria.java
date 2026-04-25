package com.meuprojeto.model;

/**
 * CLASSE: Categoria
 * FUNÇÃO: Representa a classificação de um produto (ex: Bebidas, Limpeza, Peças).
 * EXPLICAÇÃO PARA O FRONT-END: Este é o "molde" dos dados que preenchem o Dropdown
 * (Select) no formulário de cadastro de produtos. Quando o Front-end recebe uma
 * lista de categorias, cada objeto terá exatamente este formato JSON.
 */
public class Categoria {

    // ID único da categoria no banco de dados (chave primária)
    private int id;

    // Nome descritivo da categoria
    private String nome;

    // Construtor vazio: Necessário para que frameworks como o Javalin/Jackson
    // consigam converter o JSON que vem da Web automaticamente para Java.
    public Categoria() {}

    // --- GETTERS E SETTERS ---
    // Permitem que outras partes do sistema leiam ou alterem os dados desta classe.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}