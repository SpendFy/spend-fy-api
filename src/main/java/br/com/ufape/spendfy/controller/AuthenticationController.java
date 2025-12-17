package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.AuthenticationDTO;
import br.com.ufape.spendfy.dto.LoginResponseDTO;
import br.com.ufape.spendfy.dto.RegisterDTO;
import br.com.ufape.spendfy.entity.User;
import br.com.ufape.spendfy.enums.UserStatus;
import br.com.ufape.spendfy.repository.UserRepository;
import br.com.ufape.spendfy.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository repository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.senha());
        
        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO data) {
        if (repository.findByEmail(data.email()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());

        User newUser = User.builder()
                .name(data.nome())
                .email(data.email())
                .password(encryptedPassword)
                .status(UserStatus.ACTIVE)
                .build();

        repository.save(newUser);

        return ResponseEntity.ok().build();
    }
}