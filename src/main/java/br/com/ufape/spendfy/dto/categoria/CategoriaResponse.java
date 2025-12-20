package br.com.ufape.spendfy.dto.categoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaResponse {

    private Long id;
    private String nome;
    private String cor;
    private Long idUsuario;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
}
