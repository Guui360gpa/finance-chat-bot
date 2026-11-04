package br.com.guilherme.financebot.domain.valueobject;

import lombok.Getter;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;

@Getter
public final class Usuario {

    public static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");
    private static final int NOME_MAX = 100;

    private final Long id;
    private final long telegramId;
    private final String nome;
    private final ZoneId fusoHorario;
    private final Instant criadoEm;

    private Usuario(Long id, long telegramId, String nome, ZoneId fusoHorario, Instant criadoEm) {
        this.id = id;
        this.telegramId = telegramId;
        this.nome = normalizarNome(nome);
        this.fusoHorario = Objects.requireNonNull(fusoHorario, "fusoHorário não pode ser nulo");
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    public static Usuario novo(long telegramId, String nome, Instant agora) {
        return new Usuario(null,telegramId,nome,FUSO_PADRAO,agora);
    }

    public static Usuario reconstruir(Long id, long telegramId, String nome, ZoneId fusoHorario, Instant criadoEm) {
        return new Usuario(Objects.requireNonNull(id,"id não pode ser nulo"),telegramId,nome,fusoHorario,criadoEm);
    }

    private static String normalizarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String limpo = nome.strip();
        if (limpo.length() > NOME_MAX) {
            throw new IllegalArgumentException("Nome excede " + NOME_MAX + " caracteres");
        }
        return limpo;
    }




}
