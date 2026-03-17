package br.com.ufape.spendfy.entity;

public enum ContaType {
    CHECKING("Conta Corrente"),
    SAVINGS("Poupança"),
    CREDIT_CARD("Cartão de Crédito"),
    CASH("Dinheiro"),
    INVESTMENT("Investimento");
    
    private final String label;
    
    ContaType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}