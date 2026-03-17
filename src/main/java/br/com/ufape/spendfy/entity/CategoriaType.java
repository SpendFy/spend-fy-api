package br.com.ufape.spendfy.entity;

public enum CategoriaType {
    INCOME("Entrada"),
    EXPENSE("Saída");
    
    private final String label;
    
    CategoriaType(String label) {
        this.label = label;
    }
    
    public String getLabel() {
        return label;
    }
}