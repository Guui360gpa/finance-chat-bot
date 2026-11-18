package br.com.guilherme.financebot.application.port.in;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Dados necessários para registrar uma transação.
 *
 * @param usuario          usuário já persistido (com id); o fuso dele decide o que é "hoje"
 * @param data             data local do usuário (já resolvida pelo parser: "ontem" = hoje - 1)
 * @param descricao        opcional
 * @param mensagemOriginal texto digitado, guardado para auditoria
 */

public record RegistrarTransacaoComando(
        Usuario usuario,
        TipoTransacao tipo,
        Dinheiro valor,
        String descricao,
        LocalDate data,
        String mensagemOriginal
) {

    public RegistrarTransacaoComando {
        Objects.requireNonNull(usuario,"usuario não pode ser nulo");
        Objects.requireNonNull(tipo,"tipo não pode ser nulo");
        Objects.requireNonNull(valor,"valor não pode ser nulo");
        Objects.requireNonNull(data,"data não pode ser nula");
    }
}
