package br.com.ufape.spendfy.dto;

import br.com.ufape.spendfy.enums.UserStatus;

public record RegisterDTO(String name, String email, String password, UserStatus status) {
}