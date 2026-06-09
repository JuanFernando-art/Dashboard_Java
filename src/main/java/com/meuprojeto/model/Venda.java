package com.meuprojeto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Representa o registro consolidado de uma transação de venda
@JsonIgnoreProperties(ignoreUnknown = true)
public class Venda {

    private int id;
    private double total;
    private int idEmpreendimento;
    private String formaPagamento;
    private List<ItemVenda> itens;
    private String produtosVendidos;
    private String dataVenda;

    public Venda() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getIdEmpreendimento() {
        return idEmpreendimento;
    }

    public void setIdEmpreendimento(int idEmpreendimento) {
        this.idEmpreendimento = idEmpreendimento;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public List<ItemVenda> getItens() {
        return itens;
    }

    public void setItens(List<ItemVenda> itens) {
        this.itens = itens;
    }

    public String getProdutosVendidos() {
        return produtosVendidos;
    }

    public void setProdutosVendidos(String produtosVendidos) {
        this.produtosVendidos = produtosVendidos;
    }

    public String getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(String dataVenda) {
        this.dataVenda = dataVenda;
    }
}
