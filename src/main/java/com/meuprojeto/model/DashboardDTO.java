package com.meuprojeto.model;

/**
 * CLASSE: DashboardDTO (Data Transfer Object)
 * FUNÇÃO: Transportar os cálculos financeiros do Back-end para a Dashboard no Front-end.
 * EXPLICAÇÃO PARA O FRONT-END: Esta classe é o "resumo" que aparece nos cards coloridos
 * e nas barras brancas. Ela não existe como uma tabela única no MySQL, mas é o
 * resultado da soma de vendas e custos processados pelo Java.
 */
public class DashboardDTO {

    private int idEmpreendimento;

    // Nome da loja ou unidade de negócio
    private String nomeEmpreendimento;

    // Valor total de vendas realizado (Faturamento)
    private double lucroBruto;

    // Valor total que foi pago pelos produtos vendidos (Custo de mercadoria)
    private double gastoBruto;

    // O que sobra após subtrair os gastos do lucro bruto (Dinheiro real no bolso)
    private double lucroLiquido;

    // Construtor padrão necessário para a conversão de JSON
    public DashboardDTO() {}

    // --- GETTERS E SETTERS ---
    // Essenciais para o Javalin transformar este objeto em JSON automaticamente.

    public int getIdEmpreendimento() {
        return idEmpreendimento;
    }

    public void setIdEmpreendimento(int idEmpreendimento) {
        this.idEmpreendimento = idEmpreendimento;
    }

    public String getNomeEmpreendimento() {
        return nomeEmpreendimento;
    }

    public void setNomeEmpreendimento(String nomeEmpreendimento) {
        this.nomeEmpreendimento = nomeEmpreendimento;
    }

    public double getLucroBruto() {
        return lucroBruto;
    }

    public void setLucroBruto(double lucroBruto) {
        this.lucroBruto = lucroBruto;
    }

    public double getGastoBruto() {
        return gastoBruto;
    }

    public void setGastoBruto(double gastoBruto) {
        this.gastoBruto = gastoBruto;
    }

    public double getLucroLiquido() {
        return lucroLiquido;
    }

    public void setLucroLiquido(double lucroLiquido) {
        this.lucroLiquido = lucroLiquido;
    }
}
