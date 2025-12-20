package br.com.ufape.spendfy.dto.conta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContaResponse {

    private Long id;
    private String nome;
    private String tipo;
    private BigDecimal saldoInicial;
    private Long idUsuario;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
