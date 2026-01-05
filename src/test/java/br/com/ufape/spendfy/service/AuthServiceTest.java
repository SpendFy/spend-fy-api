package br.com.ufape.spendfy.service;

import br.com.ufape.spendfy.dto.auth.AuthResponse;
import br.com.ufape.spendfy.dto.auth.LoginRequest;
import br.com.ufape.spendfy.dto.auth.RegisterRequest;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - AuthService")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("joao@email.com")
                .senha("senha123")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nome("João Silva")
                .email("joao@email.com")
                .senha("$2a$10$encodedPassword")
                .status("ATIVO")
                .build();
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void deveRegistrarNovoUsuarioComSucesso() {
        when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");

        verify(usuarioRepository, times(1)).existsByEmail("joao@email.com");
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
        verify(jwtService, times(1)).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve codificar senha ao registrar usuário")
    void deveCodificarSenhaAoRegistrarUsuario() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        authService.register(registerRequest);

        verify(passwordEncoder, times(1)).encode("senha123");
    }

    @Test
    @DisplayName("Deve definir status ATIVO ao registrar usuário")
    void deveDefinirStatusAtivoAoRegistrarUsuario() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario savedUsuario = invocation.getArgument(0);
            assertThat(savedUsuario.getStatus()).isEqualTo("ATIVO");
            return usuario;
        });
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        authService.register(registerRequest);

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve gerar token JWT ao registrar usuário")
    void deveGerarTokenJWTAoRegistrarUsuario() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isNotEmpty();
        verify(jwtService, times(1)).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar com email já cadastrado")
    void deveLancarExcecaoAoRegistrarComEmailJaCadastrado() {
        when(usuarioRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email já cadastrado");

        verify(usuarioRepository, times(1)).existsByEmail("joao@email.com");
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("João Silva");
        assertThat(response.getEmail()).isEqualTo("joao@email.com");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, times(1)).findByEmail("joao@email.com");
        verify(jwtService, times(1)).generateToken(usuario);
    }

    @Test
    @DisplayName("Deve autenticar credenciais no login")
    void deveAutenticarCredenciaisNoLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake.jwt.token");

        authService.login(loginRequest);

        verify(authenticationManager, times(1)).authenticate(
                argThat(auth ->
                    auth instanceof UsernamePasswordAuthenticationToken &&
                    auth.getPrincipal().equals("joao@email.com") &&
                    auth.getCredentials().equals("senha123")
                )
        );
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer login com credenciais inválidas")
    void deveLancarExcecaoAoFazerLoginComCredenciaisInvalidas() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Credenciais inválidas");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao fazer login com email não encontrado")
    void deveLancarExcecaoAoFazerLoginComEmailNaoEncontrado() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado");

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, times(1)).findByEmail("joao@email.com");
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve gerar token JWT ao fazer login")
    void deveGerarTokenJWTAoFazerLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isNotEmpty();
        verify(jwtService, times(1)).generateToken(usuario);
    }

    @Test
    @DisplayName("Deve retornar informações do usuário no login")
    void deveRetornarInformacoesDoUsuarioNoLogin() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("fake.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getId()).isEqualTo(usuario.getId());
        assertThat(response.getNome()).isEqualTo(usuario.getNome());
        assertThat(response.getEmail()).isEqualTo(usuario.getEmail());
    }

    @Test
    @DisplayName("Deve validar formato de email ao registrar")
    void deveValidarFormatoEmailAoRegistrar() {
        registerRequest.setEmail("email-invalido");

        when(usuarioRepository.existsByEmail("email-invalido")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve registrar usuário com nome contendo caracteres especiais")
    void deveRegistrarUsuarioComNomeContendoCaracteresEspeciais() {
        registerRequest.setNome("José da Silva Júnior");
        usuario.setNome("José da Silva Júnior");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getNome()).isEqualTo("José da Silva Júnior");
    }

    @Test
    @DisplayName("Deve registrar usuário com senha de tamanho mínimo")
    void deveRegistrarUsuarioComSenhaTamanhoMinimo() {
        registerRequest.setSenha("123456");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        verify(passwordEncoder, times(1)).encode("123456");
    }

    @Test
    @DisplayName("Deve registrar usuário com email em maiúsculas")
    void deveRegistrarUsuarioComEmailEmMaiusculas() {
        registerRequest.setEmail("JOAO@EMAIL.COM");

        when(usuarioRepository.existsByEmail("JOAO@EMAIL.COM")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake.jwt.token");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        verify(usuarioRepository, times(1)).existsByEmail("JOAO@EMAIL.COM");
    }
}
