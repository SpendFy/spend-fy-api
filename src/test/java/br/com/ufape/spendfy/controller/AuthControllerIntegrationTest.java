package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.auth.LoginRequest;
import br.com.ufape.spendfy.dto.auth.RegisterRequest;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - AuthController")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        registerRequest = RegisterRequest.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha("senha123")
                .build();

        loginRequest = LoginRequest.builder()
                .email("joao@email.com")
                .senha("senha123")
                .build();
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));

        assertThat(usuarioRepository.count()).isEqualTo(1);

        Usuario usuario = usuarioRepository.findByEmail("joao@email.com").orElseThrow();
        assertThat(usuario.getStatus()).isEqualTo("ATIVO");
        assertThat(passwordEncoder.matches("senha123", usuario.getSenha())).isTrue();
    }

    @Test
    @DisplayName("Deve lançar erro ao registrar email duplicado")
    void deveLancarErroAoRegistrarEmailDuplicado() throws Exception {
        Usuario usuario = Usuario.builder()
                .nome("Maria")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("outrasenha"))
                .status("ATIVO")
                .build();
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao registrar sem campos obrigatórios")
    void deveRetornarErro400AoRegistrarSemCamposObrigatorios() throws Exception {
        RegisterRequest requestInvalido = RegisterRequest.builder().build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro ao registrar com senha menor que 6 caracteres")
    void deveRetornarErroAoRegistrarComSenhaCurta() throws Exception {
        registerRequest.setSenha("12345");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar erro ao registrar com email inválido")
    void deveRetornarErroAoRegistrarComEmailInvalido() throws Exception {
        registerRequest.setEmail("email-invalido");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() throws Exception {
        Usuario usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    @DisplayName("Deve retornar erro 401 ao fazer login com credenciais inválidas")
    void deveRetornarErro401AoFazerLoginComCredenciaisInvalidas() throws Exception {
        Usuario usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuarioRepository.save(usuario);

        loginRequest.setSenha("senhaerrada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar erro 401 ao fazer login com email não cadastrado")
    void deveRetornarErro401AoFazerLoginComEmailNaoCadastrado() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao fazer login sem campos obrigatórios")
    void deveRetornarErro400AoFazerLoginSemCamposObrigatorios() throws Exception {
        LoginRequest requestInvalido = LoginRequest.builder().build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve registrar usuário com nome contendo caracteres especiais")
    void deveRegistrarUsuarioComNomeContendoCaracteresEspeciais() throws Exception {
        registerRequest.setNome("José da Silva Júnior");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("José da Silva Júnior"));
    }

    @Test
    @DisplayName("Deve validar que senha é codificada no registro")
    void deveValidarQueSenhaECodificadaNoRegistro() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        Usuario usuario = usuarioRepository.findByEmail("joao@email.com").orElseThrow();

        // Senha não deve estar em texto plano
        assertThat(usuario.getSenha()).isNotEqualTo("senha123");
        // Deve estar codificada com BCrypt
        assertThat(usuario.getSenha()).startsWith("$2a$");
        // Deve validar corretamente
        assertThat(passwordEncoder.matches("senha123", usuario.getSenha())).isTrue();
    }
}
