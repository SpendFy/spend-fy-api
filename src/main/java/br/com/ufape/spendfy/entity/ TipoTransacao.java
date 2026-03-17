package br.com.ufape.spendfy.entity;

public enum TipoTransacao {
    INCOME("Receita"),
    EXPENSE("Despesa"),
    TRANSFER("Transferência");
    
    private final String label;
    
    TipoTransacao(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}