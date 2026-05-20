package com.meuprojeto.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CLASSE: ContaPagamento
 * FUNÇÃO: Representa uma conta bancária ou chave PIX configurada para um empreendimento.
 * EXPLICAÇÃO PARA O FRONT-END: Este objeto armazena os detalhes de uma conta
 * (banco, agência, número, tipo) e a chave PIX associada, permitindo que o
 * sistema saiba para onde direcionar pagamentos ou exibir informações de PIX.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContaPagamento {

    private int idConta;
    private String nomeBanco;
    private String agencia;
    private String conta;
    private String tipoConta; // Ex: Corrente, Poupança
    private String pixChave;
    private boolean ativaConta; // Se o número da conta está ativo
    private boolean ativaPix;   // Se a chave PIX está ativa
    private int idEmpreendimento;

    public ContaPagamento() {}

    // --- GETTERS E SETTERS ---

    public int getIdConta() {
        return idConta;
    }

    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }

    public String getNomeBanco() {
        return nomeBanco;
    }

    public void setNomeBanco(String nomeBanco) {
        this.nomeBanco = nomeBanco;
    }

    public String getAgencia() {
        return agencia;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public String getTipoConta() {
        return tipoConta;
    }

    public void setTipoConta(String tipoConta) {
        this.tipoConta = tipoConta;
    }

    public String getPixChave() {
        return pixChave;
    }

    public void setPixChave(String pixChave) {
        this.pixChave = pixChave;
    }

    public boolean isAtivaConta() {
        return ativaConta;
    }

    public void setAtivaConta(boolean ativaConta) {
        this.ativaConta = ativaConta;
    }

    public boolean isAtivaPix() {
        return ativaPix;
    }

    public void setAtivaPix(boolean ativaPix) {
        this.ativaPix = ativaPix;
    }

    public int getIdEmpreendimento() {
        return idEmpreendimento;
    }

    public void setIdEmpreendimento(int idEmpreendimento) {
        this.idEmpreendimento = idEmpreendimento;
    }
}