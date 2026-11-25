package br.com.guilherme.financebot.application.port.in;


import br.com.guilherme.financebot.domain.model.Transacao;

/** Porta de entrada: o que a aplicação oferece para registrar uma despesa ou receita. */
public interface RegistrarTransacaoUseCase {

    //Registra a transação, avisa o usuário e devolve a transação salva.
    Transacao registrar(RegistrarTransacaoComando comando);
}
