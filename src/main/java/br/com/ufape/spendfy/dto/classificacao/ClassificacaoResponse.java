package br.com.ufape.spendfy.dto.classificacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassificacaoResponse {
    private Long idCategoria;
    private String nomeCategoria;
    private String cor;
    private String justificativa;
}
