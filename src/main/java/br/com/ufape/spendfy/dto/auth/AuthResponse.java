package br.com.ufape.spendfy.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;
    private String tipo;
    private Long id;
    private String nome;
    private String email;

    public AuthResponse(String token, Long id, String nome, String email) {
        this.token = token;
        this.tipo = "Bearer";
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}
