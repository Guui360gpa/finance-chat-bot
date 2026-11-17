package br.com.guilherme.financebot.application.parser;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Despesa ou receita extraída de um texto livre.
 * A data é a data local do usuário (já resolvida: "ontem" vira hoje - 1).
 */

public record GastoInterpretado(
        TipoTransacao tipo,
        Dinheiro valor,
        String descricao,
        LocalDate data
) implements MensagemInterpretada {

    public GastoInterpretado {
        Objects.requireNonNull(tipo,"tipo não pode ser nulo");
        Objects.requireNonNull(valor, "valor não pode ser nulo");
        Objects.requireNonNull(data, "data não pode ser nula");
    }
}
