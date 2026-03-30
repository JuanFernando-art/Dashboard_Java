package com.meuprojeto.model;

public class Produto {
    private int id;
    private String nome;
    private String categoria;
    private double precoCusto;
    private double precoVenda;
    private int quantidade;

    // Construtor vazio (importante para o Java)
    public Produto() {
    }

    // Getters e Setters (Os controles para ler e escrever os dados)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }

    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
}