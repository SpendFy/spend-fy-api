package br.com.ufape.spendfy.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "contas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conta")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Transacao> transacoes = new ArrayList<>();
}