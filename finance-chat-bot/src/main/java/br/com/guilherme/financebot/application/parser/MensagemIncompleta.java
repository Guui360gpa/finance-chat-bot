package br.com.guilherme.financebot.application.parser;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;

import java.util.Objects;

/**
 * Havia um valor, mas faltou a descrição (ex.: "gastei 45").
 * Decisão do projeto: rejeitar e avisar o usuário, porque sem descrição não existe palavra-chave
 * para categorizar nem para aprender (regra_categoria.palavra_chave é NOT NULL).
 * Carrega o valor para o aviso poder dizer "entendi R$ 45,00, mas faltou a descrição".
 */

public record MensagemIncompleta(
        Dinheiro valor
) implements MensagemInterpretada{

    public MensagemIncompleta {
        Objects.requireNonNull(valor,"valor não pode ser nulo");
    }
}
