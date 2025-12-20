package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.auth.AuthResponse;
import br.com.ufape.spendfy.dto.auth.LoginRequest;
import br.com.ufape.spendfy.dto.auth.RegisterRequest;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .status("ATIVO")
                .build();

        usuario = usuarioRepository.save(usuario);

        String jwtToken = jwtService.generateToken(usuario);

        return new AuthResponse(jwtToken, usuario.getId(), usuario.getNome(), usuario.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        String jwtToken = jwtService.generateToken(usuario);

        return new AuthResponse(jwtToken, usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}
