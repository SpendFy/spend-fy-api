package br.com.ufape.spendfy.dto.conta;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContaRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotBlank(message = "Tipo é obrigatório")
    @Size(max = 30, message = "Tipo deve ter no máximo 30 caracteres")
    private String tipo;

    @NotNull(message = "Saldo inicial é obrigatório")
    @DecimalMin(value = "0.0", message = "Saldo inicial não pode ser negativo")
    private BigDecimal saldoInicial;
}
