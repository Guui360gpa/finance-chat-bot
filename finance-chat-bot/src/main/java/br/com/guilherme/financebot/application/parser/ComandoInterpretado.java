package br.com.guilherme.financebot.application.parser;

import java.util.Objects;

/** Ex.: "/orcamento alimentação 800" -> ORCAMENTO com argumentos "alimentação 800". */

public record ComandoInterpretado(
        Comando comando,
        String argumentos
) implements MensagemInterpretada {

    public ComandoInterpretado {
        Objects.requireNonNull(comando, "comando não pode ser nulo");
        argumentos = argumentos == null ? "" : argumentos;
    }
}
