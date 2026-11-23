package br.com.guilherme.financebot.application.port.in;

/** Porta de entrada: o que o canal (Telegram) chama para cada mensagem recebida. */
public interface ProcessarMensagemUseCase {

    void processar(MensagemRecebida mensagem);
}
