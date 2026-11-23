package br.com.guilherme.financebot.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

/** Representação da tabela "usuario" (migration V1). O fuso é guardado como texto (ex.: America/Sao_Paulo). */
@Entity
@Table(name = "usuario")
@Getter
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private long telegramId;

    @Column(length = 100)
    private String nome;

    @Column(name = "fuso_horario",nullable = false,length = 40)
    private String fusoHorario;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected UsuarioEntity() {}

    public UsuarioEntity(long id, long telegramId, String nome, String fusoHorario, Instant criadoEm) {
        this.id = id;
        this.telegramId = telegramId;
        this.nome = nome;
        this.fusoHorario = fusoHorario;
        this.criadoEm = criadoEm;
    }
}
