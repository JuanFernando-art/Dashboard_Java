package com.meuprojeto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * CLASSE: Venda
 * FUNÇÃO: Representa o registro histórico de uma transação concluída.
 * EXPLICAÇÃO PARA O FRONT-END: Este objeto é usado para salvar o resultado final de
 * um carrinho de compras. Ele consolida o valor total e uma descrição textual
 * dos itens para que o histórico de vendas possa ser consultado sem precisar
 * de cálculos complexos a cada visualização.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Venda {

    // Identificador único da venda (Gerado automaticamente pelo MySQL)
    private int id;

    // Valor total bruto da venda (Soma de todos os itens vendidos)
    private double total;

    private int idEmpreendimento;

    private List<ItemVenda> itens;

    // Lista descritiva dos produtos.
    // DICA PARA O FRONT-END: Você pode formatar isso como uma String simples
    // ex: "Martelo (2x), Prego 10kg (1x)". Isso facilita a exibição rápida no histórico.
    private String produtosVendidos;

    // Data e hora em que a venda foi realizada.
    // No banco de dados, geralmente é preenchido automaticamente (CURRENT_TIMESTAMP).
    private String dataVenda;

    // Construtor padrão necessário para serialização JSON
    public Venda() {}

    // --- GETTERS E SETTERS ---
    // Métodos para o Javalin acessar os dados durante a conversão para JSON.

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
