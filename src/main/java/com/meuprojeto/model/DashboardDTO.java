package com.meuprojeto.model;

// Objeto de transferência para os indicadores financeiros do painel principal
public class DashboardDTO {

    private int idEmpreendimento;
    private String nomeEmpreendimento;
    private double lucroBruto;
    private double gastoBruto;
    private double lucroLiquido;

    public DashboardDTO() {}

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
