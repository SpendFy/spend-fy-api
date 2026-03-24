package br.com.ufape.spendfy.dto.transacao;

import br.com.ufape.spendfy.entity.enums.StatusTransacao;
import br.com.ufape.spendfy.entity.enums.TipoTransacao;
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
public class TransacaoResponse {

    private Long id;
    private TipoTransacao tipo;
    private LocalDate data;
    private BigDecimal valor;
    private String descricao;
    private String observacao;
    private StatusTransacao status;
    private Long idUsuario;
    private Long idConta;
    private String nomeConta;
    private Long idCategoria;
    private String nomeCategoria;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
