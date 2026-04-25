package com.meuprojeto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CLASSE: Produto
 * FUNÇÃO: Representa um item do estoque no sistema.
 * EXPLICAÇÃO PARA O FRONT-END: Este objeto contém tanto os dados comerciais (preços)
 * quanto os dados de controle de estoque (quantidades). A anotação @JsonIgnoreProperties
 * garante que, se o Front-end enviar algum campo extra que o Java não conhece,
 * o sistema não vai travar.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Produto {

    // Identificador único do produto no banco de dados
    private int id;

    // Nome do produto (ex: "Filtro de Óleo", "Pão de Forma")
    private String nome;

    // Valor pago pelo lojista ao fornecedor (usado para calcular o lucro líquido)
    private double precoCusto;

    // Valor cobrado do cliente final (usado para calcular o faturamento bruto)
    private double precoVenda;

    // Quantidade atual disponível nas prateleiras
    private int quantidade;

    // Quantidade que havia no estoque quando o produto foi cadastrado ou reabastecido.
    // DICA PARA O FRONT-END: Use este campo junto com o 'quantidade' para criar a barra de progresso.
    private int quantidadeInicial;

    // Vínculo com a categoria (ex: 1 para 'Limpeza', 2 para 'Alimentos')
    private int idCategoria;

    // Vínculo com a loja dona deste produto (ex: 1 para 'Oficina', 2 para 'Padaria')
    private int idEmpreendimento;

    // Construtor padrão necessário para a biblioteca Jackson converter JSON <-> Java
    public Produto() {}

    // --- GETTERS E SETTERS ---
    // Métodos de acesso para leitura e escrita dos dados.

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