package com.meuprojeto.model;

public class Venda {
    private int id;
    private double total;
    private String produtosVendidos; // Uma lista em texto: "Mouse (2), Teclado (1)"
    private String dataVenda;

    public Venda() {}

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getProdutosVendidos() { return produtosVendidos; }
    public void setProdutosVendidos(String produtosVendidos) { this.produtosVendidos = produtosVendidos; }

    public String getDataVenda() { return dataVenda; }
    public void setDataVenda(String dataVenda) { this.dataVenda = dataVenda; }
}