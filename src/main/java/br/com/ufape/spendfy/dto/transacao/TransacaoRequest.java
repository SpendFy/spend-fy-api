package br.com.ufape.spendfy.dto.transacao;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransacaoRequest {

    @NotBlank(message = "Tipo é obrigatório")
    @Size(max = 20, message = "Tipo deve ter no máximo 20 caracteres")
    private String tipo;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    private String descricao;

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    private String observacao;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 20, message = "Status deve ter no máximo 20 caracteres")
    private String status;

    @NotNull(message = "ID da conta é obrigatório")
    private Long idConta;

    @NotNull(message = "ID da categoria é obrigatório")
    private Long idCategoria;
}
