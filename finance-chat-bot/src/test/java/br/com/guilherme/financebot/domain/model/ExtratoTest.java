package br.com.guilherme.financebot.domain.model;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ExtratoTest {

    private static Transacao transacao(TipoTransacao tipo, String valor, String instante) {
        Instant momento = Instant.parse(instante);
        return Transacao.registrar(1L, tipo, Dinheiro.de(valor), "x", momento, "m", momento);
    }

    @Test
    void somaReceitasDespesasECalculaOSaldo() {
        var extrato = new Extrato(List.of(
                transacao(TipoTransacao.RECEITA, "3000", "2026-03-01T12:00:00Z"),
                transacao(TipoTransacao.DESPESA, "45", "2026-03-01T13:00:00Z"),
                transacao(TipoTransacao.DESPESA, "23.50", "2026-03-01T14:00:00Z")));

        assertThat(extrato.totalReceitas()).isEqualTo(Dinheiro.de("3000.00"));
        assertThat(extrato.totalDespesas()).isEqualTo(Dinheiro.de("68.50"));
        assertThat(extrato.saldo()).isEqualTo(Dinheiro.de("2931.50"));
    }

    @Test
    void saldoPodeSerNegativo() {
        var extrato = new Extrato(List.of(transacao(TipoTransacao.DESPESA, "80", "2026-03-01T12:00:00Z")));

        assertThat(extrato.saldo()).isEqualTo(Dinheiro.de("-80.00"));
        assertThat(extrato.saldo().ehNegativo()).isTrue();
    }

    @Test
    void ignoraTransacoesDesfeitas() {
        Transacao desfeita = transacao(TipoTransacao.DESPESA, "999", "2026-03-01T12:00:00Z");
        desfeita.desfazer(Instant.parse("2026-03-01T12:05:00Z"));
        var extrato = new Extrato(List.of(desfeita, transacao(TipoTransacao.DESPESA, "10", "2026-03-01T13:00:00Z")));

        assertThat(extrato.transacoes()).hasSize(1);
        assertThat(extrato.totalDespesas()).isEqualTo(Dinheiro.de("10.00"));
    }

    @Test
    void ordenaDaMaisAntigaParaAMaisRecente() {
        Transacao tarde = transacao(TipoTransacao.DESPESA, "2", "2026-03-01T18:00:00Z");
        Transacao cedo = transacao(TipoTransacao.DESPESA, "1", "2026-03-01T08:00:00Z");

        var extrato = new Extrato(List.of(tarde, cedo));

        assertThat(extrato.transacoes()).containsExactly(cedo, tarde);
    }

    @Test
    void extratoVazioTemTotaisZerados() {
        var extrato = new Extrato(List.of());

        assertThat(extrato.estaVazio()).isTrue();
        assertThat(extrato.totalReceitas().ehZero()).isTrue();
        assertThat(extrato.totalDespesas().ehZero()).isTrue();
        assertThat(extrato.saldo().ehZero()).isTrue();
    }

}