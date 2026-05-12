package com.meuprojeto.model;

public class Categoria {
    private int idCategoria;
    private String nome;
    private Integer idCategoriaPai;

    public Categoria() {}

    public Categoria(int idCategoria, String nome, Integer idCategoriaPai) {
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.idCategoriaPai = idCategoriaPai;
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

    @Override
    public String toString() {
        return "Categoria{" +
               "idCategoria=" + idCategoria +
               ", nome='" + nome + '\'' +
               ", idCategoriaPai=" + idCategoriaPai +
               '}';
    }
}