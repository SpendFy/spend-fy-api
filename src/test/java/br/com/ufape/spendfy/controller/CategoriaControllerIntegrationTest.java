package br.com.ufape.spendfy.controller;

import br.com.ufape.spendfy.dto.categoria.CategoriaRequest;
import br.com.ufape.spendfy.entity.Categoria;
import br.com.ufape.spendfy.entity.Usuario;
import br.com.ufape.spendfy.repository.CategoriaRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração - CategoriaController")
class CategoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private CategoriaRequest categoriaRequest;

    @BeforeEach
    void setUp() {
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = Usuario.builder()
                .nome("João Silva")
                .email("joao@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .status("ATIVO")
                .build();
        usuario = usuarioRepository.save(usuario);

        categoriaRequest = CategoriaRequest.builder()
                .nome("Alimentação")
                .cor("verde")
                .build();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar categoria com sucesso")
    void deveCriarCategoriaComSucesso() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Alimentação"))
                .andExpect(jsonPath("$.cor").value("verde"));

        assertThat(categoriaRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve criar categoria sem cor")
    void deveCriarCategoriaSemCor() throws Exception {
        categoriaRequest.setCor(null);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Alimentação"))
                .andExpect(jsonPath("$.cor").isEmpty());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve lançar erro ao criar categoria com nome duplicado")
    void deveLancarErroAoCriarCategoriaComNomeDuplicado() throws Exception {
        Categoria categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoriaRepository.save(categoria);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Já existe uma categoria com este nome"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve listar todas as categorias do usuário")
    void deveListarTodasCategoriasDoUsuario() throws Exception {
        Categoria categoria1 = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoriaRepository.save(categoria1);

        Categoria categoria2 = Categoria.builder()
                .nome("Transporte")
                .cor("azul")
                .usuario(usuario)
                .build();
        categoriaRepository.save(categoria2);

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Alimentação"))
                .andExpect(jsonPath("$[1].nome").value("Transporte"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve buscar categoria por ID")
    void deveBuscarCategoriaPorId() throws Exception {
        Categoria categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoria = categoriaRepository.save(categoria);

        mockMvc.perform(get("/api/categorias/{id}", categoria.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoria.getId()))
                .andExpect(jsonPath("$.nome").value("Alimentação"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve atualizar categoria com sucesso")
    void deveAtualizarCategoriaComSucesso() throws Exception {
        Categoria categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoria = categoriaRepository.save(categoria);

        categoriaRequest.setNome("Alimentação Atualizada");
        categoriaRequest.setCor("vermelho");

        mockMvc.perform(put("/api/categorias/{id}", categoria.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Alimentação Atualizada"))
                .andExpect(jsonPath("$.cor").value("vermelho"));
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve deletar categoria com sucesso")
    void deveDeletarCategoriaComSucesso() throws Exception {
        Categoria categoria = Categoria.builder()
                .nome("Alimentação")
                .cor("verde")
                .usuario(usuario)
                .build();
        categoria = categoriaRepository.save(categoria);

        mockMvc.perform(delete("/api/categorias/{id}", categoria.getId()))
                .andExpect(status().isNoContent());

        assertThat(categoriaRepository.findById(categoria.getId())).isEmpty();
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 404 ao buscar categoria inexistente")
    void deveRetornarErro404AoBuscarCategoriaInexistente() throws Exception {
        mockMvc.perform(get("/api/categorias/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "joao@email.com")
    @DisplayName("Deve retornar erro 400 ao criar categoria sem nome")
    void deveRetornarErro400AoCriarCategoriaSemNome() throws Exception {
        CategoriaRequest requestInvalido = CategoriaRequest.builder()
                .cor("verde")
                .build();

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
