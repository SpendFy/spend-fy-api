package br.com.ufape.spendfy.services;

import br.com.ufape.spendfy.entity.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(Usuario usuario) {
        try {
            return Jwts.builder()
                    .subject(usuario.getEmail())
                    .issuer("Spendfy API")
                    .issuedAt(new Date())
                    .expiration(generateExpirationDate())
                    .signWith(getSigningKey())
                    .compact();
        } catch (JwtException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException exception) {
            return "";
        }
    }

    private Date generateExpirationDate() {
        return Date.from(LocalDateTime.now().plusHours(2)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}