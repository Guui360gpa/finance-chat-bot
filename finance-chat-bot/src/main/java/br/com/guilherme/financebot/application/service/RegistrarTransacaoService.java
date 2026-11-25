package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoComando;
import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoUseCase;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.model.Transacao;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Caso de uso: registra a transação e confirma ao usuário.
 * Salva antes de notificar: se o Telegram falhar, o dado não se perde.
 * Java puro: o Clock vem de fora e as dependências são as portas.
 */

public class RegistrarTransacaoService implements RegistrarTransacaoUseCase {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransacaoRepository transacoes;
    private final NotificadorPort notificador;
    private final Clock clock;


    public RegistrarTransacaoService(TransacaoRepository transacoes, NotificadorPort notificador, Clock clock) {
        this.transacoes = Objects.requireNonNull(transacoes,"transacoes não pode ser nulo");
        this.notificador = Objects.requireNonNull(notificador, "notificador não pode ser nulo");
        this.clock = Objects.requireNonNull(clock, "clock não pode ser nulo");
    }

    @Override
    public Transacao registrar(RegistrarTransacaoComando comando) {
        Objects.requireNonNull(comando, "comando não pode ser nulo");

        Instant agora = clock.instant();
        ZoneId fuso = comando.usuario().getFusoHorario();

        Transacao nova = Transacao.registrar(
                comando.usuario().getId(),
                comando.tipo(),
                comando.valor(),
                comando.descricao(),
                instanteDaOcorrencia(comando.data(),agora,fuso),
                comando.mensagemOriginal(),
                agora
        );

        Transacao salva = transacoes.salvar(nova);
        notificador.notificar(comando.usuario(),montarConfirmacao(salva,comando.data()));
        return salva;
    }

    private static Instant instanteDaOcorrencia(LocalDate data, Instant agora, ZoneId fuso) {
        LocalDate hoje = LocalDate.ofInstant(agora, fuso);
        if (data.equals(hoje)) {
            return agora;
        }
        return data.atTime(LocalTime.NOON).atZone(fuso).toInstant();
    }

    private static String montarConfirmacao(Transacao transacao, LocalDate data) {
        String rotulo = transacao.ehDespesa() ? "Despesa" : "Receita";
        String descricao = transacao.getDescricao() == null ? "" : " (" + transacao.getDescricao() + ")";
        return rotulo + " registrada: " + transacao.getValor().formatar() + descricao
                + " em " + data.format(DATA_BR) + ".";
    }
}
