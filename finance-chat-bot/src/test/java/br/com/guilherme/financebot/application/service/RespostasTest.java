package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.domain.model.Extrato;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RespostasTest {

    private static final LocalDate DIA = LocalDate.of(2026, 3, 1);

    private static Transacao transacao(TipoTransacao tipo, String valor, String descricao, String instante) {
        Instant momento = Instant.parse(instante);
        return Transacao.registrar(1L, tipo, Dinheiro.de(valor), descricao, momento, "m", momento);
    }

    @Test
    void diaSemTransacoes() {
        assertThat(Respostas.extratoDoDia(DIA, new Extrato(List.of())))
                .isEqualTo("Nenhuma transação registrada hoje (01/03/2026).");
    }

    @Test
    void diaComTransacoesListaItensETotais() {
        var extrato = new Extrato(List.of(
                transacao(TipoTransacao.DESPESA, "45", "mercado", "2026-03-01T12:00:00Z"),
                transacao(TipoTransacao.RECEITA, "3000", "salário", "2026-03-01T13:00:00Z")));

        assertThat(Respostas.extratoDoDia(DIA, extrato)).isEqualTo(
                "Hoje (01/03/2026):\n"
                        + "- Despesa: R$ 45,00 (mercado)\n"
                        + "- Receita: R$ 3.000,00 (salário)\n"
                        + "\n"
                        + "Receitas: R$ 3.000,00\n"
                        + "Despesas: R$ 45,00\n"
                        + "Saldo: R$ 2.955,00");
    }

    @Test
    void saldoNegativoEhMostradoComSinal() {
        var extrato = new Extrato(List.of(transacao(TipoTransacao.DESPESA, "80", "farmácia", "2026-03-01T12:00:00Z")));

        assertThat(Respostas.extratoDoDia(DIA, extrato)).endsWith("Saldo: -R$ 80,00");
    }

    @Test
    void transacaoSemDescricaoNaoMostraParenteses() {
        var extrato = new Extrato(List.of(transacao(TipoTransacao.DESPESA, "10", null, "2026-03-01T12:00:00Z")));

        assertThat(Respostas.extratoDoDia(DIA, extrato)).contains("\n- Despesa: R$ 10,00\n");
    }

}