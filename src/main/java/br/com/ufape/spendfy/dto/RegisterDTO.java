package br.com.ufape.spendfy.dto;

import br.com.ufape.spendfy.enums.UserStatus;

public record RegisterDTO(String nome, String email, String senha, UserStatus status) {
}