package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.parser.ComandoParser;
import br.com.guilherme.financebot.application.parser.GastoRegexParser;
import br.com.guilherme.financebot.application.parser.InterpretadorMensagem;
import br.com.guilherme.financebot.application.port.in.MensagemRecebida;
import br.com.guilherme.financebot.application.port.out.NotificacaoFalhouException;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.testsupport.NotificadorFalso;
import br.com.guilherme.financebot.testsupport.TransacaoRepositoryEmMemoria;
import br.com.guilherme.financebot.testsupport.UsuarioRepositoryEmMemoria;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ProcessarMensagemServiceTest {

    private static final String MEIO_DIA_EM_SP = "2026-03-01T15:00:00Z"; // 12:00 em São Paulo

    private final UsuarioRepositoryEmMemoria usuarios = new UsuarioRepositoryEmMemoria();
    private final TransacaoRepositoryEmMemoria transacoes = new TransacaoRepositoryEmMemoria();
    private final NotificadorFalso notificador = new NotificadorFalso();

    private ProcessarMensagemService servico(String instante) {
        return servico(instante, notificador);
    }

    private ProcessarMensagemService servico(String instante, NotificadorPort canal) {
        Clock clock = Clock.fixed(Instant.parse(instante), ZoneOffset.UTC);
        return new ProcessarMensagemService(
                new ObterOuCriarUsuario(usuarios, clock),
                new InterpretadorMensagem(List.of(new ComandoParser(), new GastoRegexParser())),
                new RegistrarTransacaoService(transacoes, canal, clock),
                new ConsultarExtratoDoDiaService(transacoes),
                canal,
                clock);
    }

    private MensagemRecebida mensagem(String texto) {
        return new MensagemRecebida(1L, 999L, "Guilherme", texto);
    }

    private String ultimaResposta() {
        List<NotificadorFalso.Envio> enviados = notificador.enviados();
        return enviados.get(enviados.size() - 1).mensagem();
    }

    @Test
    void primeiroContatoCriaOUsuarioERegistraOGasto() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("gastei 45 no mercado"));

        assertThat(usuarios.total()).isEqualTo(1);
        Long usuarioId = usuarios.buscarPorTelegramId(999L).orElseThrow().getId();
        assertThat(transacoes.todas()).singleElement().satisfies(t -> {
            assertThat(t.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(t.getTipo()).isEqualTo(TipoTransacao.DESPESA);
            assertThat(t.getValor()).isEqualTo(Dinheiro.de("45.00"));
            assertThat(t.getDescricao()).isEqualTo("mercado");
            assertThat(t.getMensagemOriginal()).isEqualTo("gastei 45 no mercado");
        });
        assertThat(ultimaResposta()).isEqualTo("Despesa registrada: R$ 45,00 (mercado) em 01/03/2026.");
    }

    @Test
    void mensagensSeguintesReaproveitamOUsuario() {
        var servico = servico(MEIO_DIA_EM_SP);

        servico.processar(mensagem("gastei 45 no mercado"));
        servico.processar(mensagem("uber 23,50"));

        assertThat(usuarios.total()).isEqualTo(1);
        assertThat(transacoes.todas()).hasSize(2);
        assertThat(notificador.enviados()).hasSize(2);
    }

    @Test
    void registraReceitaQuandoHaVerboDeReceita() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("recebi 3000 salário"));

        assertThat(transacoes.todas()).singleElement()
                .extracting(Transacao::getTipo).isEqualTo(TipoTransacao.RECEITA);
        assertThat(ultimaResposta()).startsWith("Receita registrada: R$ 3.000,00");
    }

    @Test
    void ontemEhCalculadoNoFusoDoUsuario() {
        // Em UTC já é 01/03 02:00, mas em São Paulo ainda é 28/02 23:00: "ontem" é 27/02.
        servico("2026-03-01T02:00:00Z").processar(mensagem("paguei 89,90 na farmácia ontem"));

        assertThat(transacoes.todas()).singleElement()
                .extracting(Transacao::getOcorridaEm).isEqualTo(Instant.parse("2026-02-27T15:00:00Z"));
        assertThat(ultimaResposta()).endsWith("em 27/02/2026.");
    }

    @Test
    void valorSemDescricaoEhRejeitadoComAviso() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("gastei 45"));

        assertThat(transacoes.todas()).isEmpty();
        assertThat(ultimaResposta()).isEqualTo(Respostas.valorSemDescricao(Dinheiro.de("45")));
    }

    @Test
    void mensagemNaoReconhecidaRecebeAjuda() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("bom dia"));

        assertThat(transacoes.todas()).isEmpty();
        assertThat(ultimaResposta()).isEqualTo(Respostas.AJUDA);
    }

    @Test
    void comandoStartRecebeAjuda() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("/start"));

        assertThat(ultimaResposta()).isEqualTo(Respostas.AJUDA);
    }

    @Test
    void comandoAindaNaoImplementadoAvisaOUsuario() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("/resumo"));

        assertThat(transacoes.todas()).isEmpty();
        assertThat(ultimaResposta()).isEqualTo(Respostas.COMANDO_INDISPONIVEL);
    }

    @Test
    void valorAcimaDoLimiteRecebeAvisoAmigavel() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("gastei 99999999999 café"));

        assertThat(transacoes.todas()).isEmpty();
        assertThat(ultimaResposta()).isEqualTo(Respostas.valorAcimaDoLimite());
    }

    @Test
    void comandoHojeMostraAsTransacoesDoDiaComTotais() {
        var servico = servico(MEIO_DIA_EM_SP);
        servico.processar(mensagem("gastei 45 no mercado"));
        servico.processar(mensagem("recebi 3000 salário"));

        servico.processar(mensagem("/hoje"));

        assertThat(ultimaResposta()).isEqualTo(
                "Hoje (01/03/2026):\n"
                        + "- Despesa: R$ 45,00 (mercado)\n"
                        + "- Receita: R$ 3.000,00 (salário)\n"
                        + "\n"
                        + "Receitas: R$ 3.000,00\n"
                        + "Despesas: R$ 45,00\n"
                        + "Saldo: R$ 2.955,00");
    }

    @Test
    void comandoHojeSemTransacoesAvisaQueEstaVazio() {
        servico(MEIO_DIA_EM_SP).processar(mensagem("/hoje"));

        assertThat(ultimaResposta()).isEqualTo("Nenhuma transação registrada hoje (01/03/2026).");
    }

    @Test
    void hojeRespeitaOFusoDoUsuario() {
        // 02:00Z ainda é 28/02 em São Paulo; duas horas depois (04:00Z) já é 01/03 lá.
        servico("2026-03-01T02:00:00Z").processar(mensagem("gastei 45 no mercado"));

        servico("2026-03-01T04:00:00Z").processar(mensagem("/hoje"));

        // Pela data em UTC os dois momentos são 01/03; pelo fuso do usuário, o gasto é de ontem.
        assertThat(ultimaResposta()).isEqualTo("Nenhuma transação registrada hoje (01/03/2026).");
    }

    @Test
    void falhaAoNotificarNaoDerrubaOProcessamentoNemPerdeOGasto() {
        NotificadorPort telegramFora = (usuario, texto) -> {
            throw new NotificacaoFalhouException("telegram fora");
        };

        assertThatCode(() -> servico(MEIO_DIA_EM_SP, telegramFora).processar(mensagem("gastei 45 no mercado")))
                .doesNotThrowAnyException();
        assertThat(transacoes.todas()).hasSize(1);
    }
}