package br.com.ufape.spendfy.dto.transacao;

import br.com.ufape.spendfy.entity.enums.RecorrenciaTransacao;
import br.com.ufape.spendfy.entity.enums.StatusTransacao;
import br.com.ufape.spendfy.entity.enums.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Tipo é obrigatório")
    private TipoTransacao tipo;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    private String descricao;

    @Size(max = 255, message = "Observação deve ter no máximo 255 caracteres")
    private String observacao;

    @NotNull(message = "Status é obrigatório")
    private StatusTransacao status;

    @NotNull(message = "ID da conta é obrigatório")
    private Long idConta;

    @NotNull(message = "ID da categoria é obrigatório")
    private Long idCategoria;

    private RecorrenciaTransacao recorrencia;
}
