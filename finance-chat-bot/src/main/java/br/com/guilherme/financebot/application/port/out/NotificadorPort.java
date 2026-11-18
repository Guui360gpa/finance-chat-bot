package br.com.guilherme.financebot.application.port.out;

import br.com.guilherme.financebot.domain.valueobject.Usuario;

/**
 * Porta de saída: enviar uma mensagem ao usuário. A aplicação não sabe qual é o canal;
 * hoje o adapter é o TelegramNotificador, amanhã pode ser WhatsApp.
 */

public interface NotificadorPort {

    void notificar(Usuario destinatario, String mensagem);
}
