package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoComando;
import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoUseCase;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.valueobject.Transacao;
import br.com.guilherme.financebot.testsupport.NotificadorFalso;
import br.com.guilherme.financebot.testsupport.TransacaoRepositoryEmMemoria;

import java.time.Clock;

public class RegistrarTransacaoService implements RegistrarTransacaoUseCase {


    public RegistrarTransacaoService(TransacaoRepository repositorio, NotificadorPort notificador, Clock fixed) {
    }

    @Override
    public Transacao registrar(RegistrarTransacaoComando comando) {
        return null;
    }


}
