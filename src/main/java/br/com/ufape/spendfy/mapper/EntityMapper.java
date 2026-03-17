package br.com.ufape.spendfy.mapper;

import br.com.ufape.spendfy.dto.auth.UserCreateDTO;
import br.com.ufape.spendfy.dto.auth.UserResponseDTO;
import br.com.ufape.spendfy.dto.categoria.CategoriaDTO;
import br.com.ufape.spendfy.dto.conta.ContaDTO;
import br.com.ufape.spendfy.dto.transacao.TransacaoDTO;
import br.com.ufape.spendfy.dto.orcamento.OrcamentoDTO;
import br.com.ufape.spendfy.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {
    
    public UserResponseDTO toUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getActive(),
            user.getCreatedAt()
        );
    }
    
    public CategoriaDTO toCategoriaDTO(Categoria categoria) {
        return new CategoriaDTO(
            categoria.getId(),
            categoria.getName(),
            categoria.getDescription(),
            categoria.getColor(),
            categoria.getType().name(),
            categoria.getCreatedAt(),
            categoria.getUpdatedAt()
        );
    }
    
    public Categoria toCategoriaEntity(CategoriaDTO dto, User user) {
        return Categoria.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .color(dto.getColor())
            .type(CategoriaType.valueOf(dto.getType()))
            .user(user)
            .build();
    }
    
    public ContaDTO toContaDTO(Conta conta) {
        return new ContaDTO(
            conta.getId(),
            conta.getName(),
            conta.getDescription(),
            conta.getBalance(),
            conta.getInitialBalance(),
            conta.getType().name(),
            conta.getActive(),
            conta.getCreatedAt(),
            conta.getUpdatedAt()
        );
    }
    
    public Conta toContaEntity(ContaDTO dto, User user) {
        return Conta.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .balance(dto.getBalance() != null ? dto.getBalance() : java.math.BigDecimal.ZERO)
            .initialBalance(dto.getInitialBalance())
            .type(ContaType.valueOf(dto.getType()))
            .active(dto.getActive() != null ? dto.getActive() : true)
            .user(user)
            .build();
    }
    
    public TransacaoDTO toTransacaoDTO(Transacao transacao) {
        return new TransacaoDTO(
            transacao.getId(),
            transacao.getDescription(),
            transacao.getAmount(),
            transacao.getType().name(),
            transacao.getTransactionDate(),
            transacao.getConta().getId(),
            transacao.getCategoria().getId(),
            transacao.getNotes(),
            transacao.getReconciled(),
            transacao.getCreatedAt(),
            transacao.getUpdatedAt()
        );
    }
    
    public Transacao toTransacaoEntity(TransacaoDTO dto, Conta conta, Categoria categoria, User user) {
        return Transacao.builder()
            .description(dto.getDescription())
            .amount(dto.getAmount())
            .type(TipoTransacao.valueOf(dto.getType()))
            .transactionDate(dto.getTransactionDate())
            .conta(conta)
            .categoria(categoria)
            .user(user)
            .notes(dto.getNotes())
            .reconciled(dto.getReconciled() != null ? dto.getReconciled() : false)
            .build();
    }
    
    public OrcamentoDTO toOrcamentoDTO(Orcamento orcamento) {
        return new OrcamentoDTO(
            orcamento.getId(),
            orcamento.getName(),
            orcamento.getLimitAmount(),
            orcamento.getStartDate(),
            orcamento.getEndDate(),
            orcamento.getCategoria().getId(),
            orcamento.getDescription(),
            orcamento.getCreatedAt(),
            orcamento.getUpdatedAt()
        );
    }
    
    public Orcamento toOrcamentoEntity(OrcamentoDTO dto, Categoria categoria, User user) {
        return Orcamento.builder()
            .name(dto.getName())
            .limitAmount(dto.getLimitAmount())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .categoria(categoria)
            .user(user)
            .description(dto.getDescription())
            .build();
    }
}