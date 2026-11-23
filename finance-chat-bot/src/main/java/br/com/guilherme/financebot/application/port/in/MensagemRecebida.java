package br.com.guilherme.financebot.application.port.in;

import java.util.Objects;

/**
 * Mensagem de texto recebida do canal, já sem detalhes do Telegram.
 *
 * @param updateId   identificador único do update no canal (base da idempotência)
 * @param telegramId quem enviou
 * @param nome       nome do remetente, opcional (usado ao criar o usuário)
 * @param texto      conteúdo digitado
 */
public record MensagemRecebida(
        long updateId,
        long telegramId,
        String nome,
        String texto
) {

    public MensagemRecebida {
        Objects.requireNonNull(texto,"texto não pode ser nulo");
    }
}
