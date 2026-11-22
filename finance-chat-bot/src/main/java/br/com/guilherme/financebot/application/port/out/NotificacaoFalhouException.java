package br.com.guilherme.financebot.application.port.out;

/**
 * A notificação não pôde ser entregue (canal fora do ar, usuário bloqueou o bot, token inválido...).
 * É o único tipo de erro que a aplicação precisa conhecer do canal: os detalhes do Telegram
 * ficam dentro do adapter.
 *
 * Propositalmente sem "cause": a exceção original do cliente HTTP pode trazer a URL completa,
 * e no Telegram o token do bot faz parte da URL. Quem a captura não deve vazar o segredo em logs.
 */
public class NotificacaoFalhouException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NotificacaoFalhouException(String message) {
        super(message);
    }
}
