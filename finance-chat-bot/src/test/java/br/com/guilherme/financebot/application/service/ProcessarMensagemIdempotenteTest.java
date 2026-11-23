package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.MensagemRecebida;
import br.com.guilherme.financebot.application.port.in.ProcessarMensagemUseCase;
import br.com.guilherme.financebot.testsupport.UpdateProcessadoRepositoryEmMemoria;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProcessarMensagemIdempotenteTest {

    private final UpdateProcessadoRepositoryEmMemoria updates = new UpdateProcessadoRepositoryEmMemoria();
    private final List<MensagemRecebida> processadas = new ArrayList<>();
    private final ProcessarMensagemUseCase delegado = processadas::add;
    private final ProcessarMensagemIdempotente casoDeUso = new ProcessarMensagemIdempotente(updates, delegado);

    private MensagemRecebida mensagem(long updateId) {
        return new MensagemRecebida(updateId, 999L, "Guilherme", "gastei 45 no mercado");
    }

    @Test
    void processaUmUpdateNovo() {
        casoDeUso.processar(mensagem(100));

        assertThat(processadas).containsExactly(mensagem(100));
    }

    @Test
    void ignoraOMesmoUpdateRecebidoDeNovo() {
        casoDeUso.processar(mensagem(100));
        casoDeUso.processar(mensagem(100));
        casoDeUso.processar(mensagem(100));

        assertThat(processadas).hasSize(1);
    }

    @Test
    void updatesDiferentesSaoTodosProcessados() {
        casoDeUso.processar(mensagem(100));
        casoDeUso.processar(mensagem(101));

        assertThat(processadas).containsExactly(mensagem(100), mensagem(101));
    }

    @Test
    void registraOUpdateAntesDeProcessar() {
        List<Boolean> jaRegistradoDuranteOProcessamento = new ArrayList<>();
        ProcessarMensagemUseCase espiao = m -> jaRegistradoDuranteOProcessamento.add(updates.contem(m.updateId()));

        new ProcessarMensagemIdempotente(updates, espiao).processar(mensagem(100));

        assertThat(jaRegistradoDuranteOProcessamento).containsExactly(true);
    }

    @Test
    void falhaNoProcessamentoSobeSemSerEngolida() {
        ProcessarMensagemUseCase quebrado = m -> {
            throw new IllegalStateException("falhou");
        };
        var comFalha = new ProcessarMensagemIdempotente(updates, quebrado);

        assertThatThrownBy(() -> comFalha.processar(mensagem(100)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falhou");
    }

}