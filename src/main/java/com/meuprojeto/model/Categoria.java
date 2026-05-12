package com.meuprojeto.model;

public class Categoria {
    private int idCategoria;
    private String nome;
    private Integer idCategoriaPai;
    private int idEmpreendimento;

    public Categoria() {}

    public Categoria(int idCategoria, String nome, Integer idCategoriaPai, int idEmpreendimento) {
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.idCategoriaPai = idCategoriaPai;
        this.idEmpreendimento = idEmpreendimento;
    }

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getIdCategoriaPai() {
        return idCategoriaPai;
    }

    public void setIdCategoriaPai(Integer idCategoriaPai) {
        this.idCategoriaPai = idCategoriaPai;
    }

    public int getIdEmpreendimento() {
        return idEmpreendimento;
    }

    public void setIdEmpreendimento(int idEmpreendimento) {
        this.idEmpreendimento = idEmpreendimento;
    }

    @Override
    public String toString() {
        return "Categoria{" +
               "idCategoria=" + idCategoria +
               ", nome='" + nome + '\'' +
               ", idCategoriaPai=" + idCategoriaPai +
               '}';
    }
}