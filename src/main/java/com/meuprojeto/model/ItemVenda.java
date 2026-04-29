package com.meuprojeto.model;

public class ItemVenda {
    private int idProduto;
    private int quantidade;
    private double precoUnitario;
    private double precoCustoNoMomento;

    public ItemVenda() {}

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public double getPrecoCustoNoMomento() {
        return precoCustoNoMomento;
    }

    public void setPrecoCustoNoMomento(double precoCustoNoMomento) {
        this.precoCustoNoMomento = precoCustoNoMomento;
    }
}
