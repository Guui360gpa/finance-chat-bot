package br.com.guilherme.financebot.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class TransacaoTest {

    private static final Instant AGORA = Instant.parse("2026-11-04T12:00:00Z");

    private Transacao despesa(String valor, String descricao) {
        return Transacao.registrar(1L,TipoTransacao.DESPESA,Dinheiro.de(valor),descricao,AGORA,"gastei " + valor,AGORA);
    }

    @Test
    void registraTransacaoValida() {
        Transacao t = despesa("45","mercado");

        assertThat(t.getId()).isNull();
        assertThat(t.getUsuarioId()).isEqualTo(1L);
        assertThat(t.getTipo()).isEqualTo(TipoTransacao.DESPESA);
        assertThat(t.getValor()).isEqualTo(Dinheiro.de("45.00"));
        assertThat(t.getDescricao()).isEqualTo("mercado");
        assertThat(t.getMensagemOriginal()).isEqualTo("gastei 45");
        assertThat(t.ehDespesa()).isTrue();
        assertThat(t.ehReceita()).isFalse();
    }

    @Test
    void nasceSemCategoriaENaoDesfeita() {
        Transacao t = despesa("45", "mercado");

        assertThat(t.getCategoriaId()).isNull();
        assertThat(t.ehDesfeita()).isFalse();
        assertThat(t.getDesfeitaEm()).isNull();
    }

    @Test
    void rejeitaValorZeroOuNegativo() {
        assertThatThrownBy(() -> despesa("0","x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> despesa("-10","x")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaCamposObrigatoriosNulos() {
        assertThatThrownBy(() -> Transacao.registrar(null,TipoTransacao.DESPESA,Dinheiro.de("1"),"x",AGORA,"m",AGORA))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Transacao.registrar(1L, null, Dinheiro.de("1"), "x", AGORA, "m", AGORA))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Transacao.registrar(1L, TipoTransacao.DESPESA, null, "x", AGORA, "m", AGORA))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Transacao.registrar(1L, TipoTransacao.DESPESA, Dinheiro.de("1"), "x", null, "m", AGORA))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void descricaoVaziaViraNulaELongaEhTruncada() {
        assertThat(despesa("1", "   ").getDescricao()).isNull();
        assertThat(despesa("1", null).getDescricao()).isNull();
        assertThat(despesa("1", "a".repeat(300)).getDescricao()).hasSize(200);
    }

    @Test
    void desfazerMarcaMomentoEImpedeSegundoDesfazer() {
        Transacao t = despesa("45", "mercado");
        Instant depois = AGORA.plusSeconds(60);

        t.desfazer(depois);

        assertThat(t.ehDesfeita()).isTrue();
        assertThat(t.getDesfeitaEm()).isEqualTo(depois);
        assertThatThrownBy(() -> t.desfazer(depois)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void categorizarAtribuiCategoria() {
        Transacao t = despesa("45", "mercado");

        t.categorizar(7L);

        assertThat(t.getCategoriaId()).isEqualTo(7L);
    }

    @Test
    void naoCategorizaTransacaoDesfeita() {
        Transacao t = despesa("45", "mercado");
        t.desfazer(AGORA);

        assertThatThrownBy(() -> t.categorizar(7L)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reconstituirPreservaTodosOsCampos() {
        Transacao t = Transacao.reconstituir(10L, 1L, 7L, TipoTransacao.RECEITA, Dinheiro.de("3000"),
                "salário", AGORA, "recebi 3000 salário", AGORA.plusSeconds(5), AGORA);

        assertThat(t.getId()).isEqualTo(10L);
        assertThat(t.getCategoriaId()).isEqualTo(7L);
        assertThat(t.ehReceita()).isTrue();
        assertThat(t.ehDesfeita()).isTrue();
    }
}
