package br.com.ufape.spendfy.dto.alerta;

import br.com.ufape.spendfy.enums.TipoAlerta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlertaResponse {
    private Long id;
    private TipoAlerta tipo;
    private String mensagem;
    private boolean lido;
    private Long idReferencia;
    private LocalDateTime criadoEm;
}
