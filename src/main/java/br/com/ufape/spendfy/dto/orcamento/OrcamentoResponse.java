package br.com.ufape.spendfy.dto.orcamento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrcamentoResponse {

    private Long id;
    private BigDecimal valorLimite;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Long idUsuario;
    private Long idCategoria;
    private String nomeCategoria;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
