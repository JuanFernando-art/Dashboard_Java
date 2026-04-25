package com.meuprojeto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Produto {
    private int id;
    private String nome;
    private double precoCusto;
    private double precoVenda;
    private int quantidade;
    private int quantidadeInicial; // Adicionado para a barra de progresso
    private int idCategoria;
    private int idEmpreendimento;

    public Produto() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }
    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public int getQuantidadeInicial() { return quantidadeInicial; }
    public void setQuantidadeInicial(int quantidadeInicial) { this.quantidadeInicial = quantidadeInicial; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public int getIdEmpreendimento() { return idEmpreendimento; }
    public void setIdEmpreendimento(int idEmpreendimento) { this.idEmpreendimento = idEmpreendimento; }
}