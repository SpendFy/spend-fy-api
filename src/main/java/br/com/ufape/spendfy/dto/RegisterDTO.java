package br.com.ufape.spendfy.dto;

import br.com.ufape.spendfy.enums.Status;

public record RegisterDTO(String nome, String email, String senha, Status status) {
}