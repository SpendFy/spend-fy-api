package br.com.ufape.spendfy.dto.insight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScoreResponse {
    private int score;
    private String classificacao;
    private List<String> fatoresPositivos;
    private List<String> fatoresNegativos;
}
