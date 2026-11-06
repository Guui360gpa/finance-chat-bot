package br.com.guilherme.financebot.domain.valueobject;

import lombok.Getter;

import java.util.Objects;

@Getter
public final class Categoria {

   private static final int NOME_MAX = 50;

   private final Long id;
   private final Long usuarioId;
   private final String nome;

    public Categoria(Long id, Long usuarioId, String nome) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId,"usuarioId não pode ser nulo");
        this.nome = validarNome(nome);
    }

    private String validarNome(String nome) {

        Objects.requireNonNull(nome,"nome não pode ser nulo");
        String limpo = nome.strip();

        if (limpo.isEmpty()) {
            throw new IllegalArgumentException("nome da categoria não pode ser vazio");
        }
        if (limpo.length() > NOME_MAX) {
            throw new IllegalArgumentException("nome da categoria excede " + NOME_MAX + " de caracteres");
        }
        return limpo;
    }
}
