package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.domain.model.Extrato;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.domain.valueobject.Usuario;
import br.com.guilherme.financebot.testsupport.TransacaoRepositoryEmMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultarExtratoDoDiaServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final ZoneId NOVA_YORK = ZoneId.of("America/New_York");

    private final TransacaoRepositoryEmMemoria transacoes = new TransacaoRepositoryEmMemoria();
    private final ConsultarExtratoDoDiaService servico = new ConsultarExtratoDoDiaService(transacoes);

    private Usuario usuario(long id, ZoneId fuso) {
        return Usuario.reconstituir(id, 900 + id, "Usuario " + id, fuso, Instant.parse("2026-01-01T00:00:00Z"));
    }

    private void registrar(long usuarioId, String instante, String valor) {
        Instant momento = Instant.parse(instante);
        transacoes.salvar(Transacao.registrar(usuarioId, TipoTransacao.DESPESA, Dinheiro.de(valor),
                "x", momento, "m", momento));
    }

    private List<String> valores(Extrato extrato) {
        return extrato.transacoes().stream().map(t -> t.getValor().valor().toPlainString()).toList();
    }

    @Test
    @DisplayName("Limites do dia em São Paulo: início incluso, fim excluso")
    void limitesDoDiaNoFusoDoUsuario() {
        registrar(1L, "2026-02-28T02:59:59Z", "1"); // 23:59:59 de 27/02 em SP: dia anterior
        registrar(1L, "2026-02-28T03:00:00Z", "2"); // 00:00:00 de 28/02 em SP: primeiro instante do dia
        registrar(1L, "2026-03-01T02:59:59Z", "3"); // 23:59:59 de 28/02 em SP: último instante do dia
        registrar(1L, "2026-03-01T03:00:00Z", "4"); // 00:00:00 de 01/03 em SP: dia seguinte

        Extrato extrato = servico.consultar(usuario(1L, SAO_PAULO), LocalDate.of(2026, 2, 28));

        assertThat(valores(extrato)).containsExactly("2.00", "3.00");
    }

    @Test
    @DisplayName("O mesmo instante cai em dias diferentes conforme o fuso")
    void mesmoInstanteEmDiasDiferentesConformeOFuso() {
        registrar(1L, "2026-03-01T02:00:00Z", "5"); // 23:00 de 28/02 em SP, mas 02:00 de 01/03 em UTC
        registrar(2L, "2026-03-01T02:00:00Z", "5");

        Usuario paulista = usuario(1L, SAO_PAULO);
        Usuario londrino = usuario(2L, ZoneId.of("UTC"));

        assertThat(servico.consultar(paulista, LocalDate.of(2026, 2, 28)).transacoes()).hasSize(1);
        assertThat(servico.consultar(paulista, LocalDate.of(2026, 3, 1)).transacoes()).isEmpty();
        assertThat(servico.consultar(londrino, LocalDate.of(2026, 3, 1)).transacoes()).hasSize(1);
        assertThat(servico.consultar(londrino, LocalDate.of(2026, 2, 28)).transacoes()).isEmpty();
    }

    @Test
    @DisplayName("Dia de horário de verão tem 23 horas: o fim não é 'início + 24h'")
    void diaDeHorarioDeVeraoTem23Horas() {
        // Em 08/03/2026 Nova York adianta o relógio: o dia vai de 05:00Z até 04:00Z do dia seguinte.
        registrar(1L, "2026-03-08T04:59:59Z", "1"); // dia anterior
        registrar(1L, "2026-03-08T05:00:00Z", "2"); // início do dia
        registrar(1L, "2026-03-09T03:59:59Z", "3"); // último instante do dia
        registrar(1L, "2026-03-09T04:00:00Z", "4"); // já é 09/03 (um intervalo de 24h erraria aqui)

        Extrato extrato = servico.consultar(usuario(1L, NOVA_YORK), LocalDate.of(2026, 3, 8));

        assertThat(valores(extrato)).containsExactly("2.00", "3.00");
    }

    @Test
    void naoMostraTransacoesDeOutrosUsuarios() {
        registrar(1L, "2026-03-01T15:00:00Z", "1");
        registrar(2L, "2026-03-01T15:00:00Z", "2");

        Extrato extrato = servico.consultar(usuario(1L, SAO_PAULO), LocalDate.of(2026, 3, 1));

        assertThat(valores(extrato)).containsExactly("1.00");
    }

    @Test
    void ignoraTransacoesDesfeitas() {
        Instant momento = Instant.parse("2026-03-01T15:00:00Z");
        Transacao desfeita = Transacao.registrar(1L, TipoTransacao.DESPESA, Dinheiro.de("9"), "x", momento, "m", momento);
        desfeita.desfazer(momento);
        transacoes.salvar(desfeita);
        registrar(1L, "2026-03-01T16:00:00Z", "1");

        Extrato extrato = servico.consultar(usuario(1L, SAO_PAULO), LocalDate.of(2026, 3, 1));

        assertThat(valores(extrato)).containsExactly("1.00");
        assertThat(extrato.totalDespesas()).isEqualTo(Dinheiro.de("1.00"));
    }

    @Test
    void diaSemTransacoesDevolveExtratoVazio() {
        assertThat(servico.consultar(usuario(1L, ZoneOffset.UTC), LocalDate.of(2026, 3, 1)).estaVazio()).isTrue();
    }

}