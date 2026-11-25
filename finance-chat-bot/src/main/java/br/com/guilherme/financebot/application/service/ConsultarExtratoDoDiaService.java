package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.ConsultarExtratoDoDiaUseCase;
import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.model.Extrato;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Converte "um dia no fuso do usuário" em um intervalo de instantes e consulta o repositório.
 *
 * O intervalo é [início do dia, início do dia seguinte), SEMIABERTO: uma transação à meia-noite
 * exata pertence ao dia que começa, e nunca aparece em dois dias. O fim é calculado com
 * plusDays(1).atStartOfDay, e não "início + 24h", porque em dias de horário de verão o dia
 * tem 23 ou 25 horas.
 *
 * Não usa Clock: quem chama informa o dia (assim o "hoje" é calculado uma vez só no fluxo).
 */
public class ConsultarExtratoDoDiaService implements ConsultarExtratoDoDiaUseCase {

    private final TransacaoRepository transacoes;

    public ConsultarExtratoDoDiaService(TransacaoRepository transacoes) {
        this.transacoes = Objects.requireNonNull(transacoes, "transacoes não pode ser nulo");
    }

    @Override
    public Extrato consultar(Usuario usuario, LocalDate data) {
        Objects.requireNonNull(usuario, "usuario não pode ser nulo");
        Objects.requireNonNull(data, "data não pode ser nula");

        ZoneId fuso = usuario.getFusoHorario();
        Instant inicio = data.atStartOfDay(fuso).toInstant();
        Instant fim = data.plusDays(1).atStartOfDay(fuso).toInstant();

        return new Extrato(transacoes.listarNoPeriodo(usuario.getId(), inicio, fim));
    }
}
