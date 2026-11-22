package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.domain.valueobject.Transacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TransacaoMapperTest {

    private static final Instant AGORA = Instant.parse("2026-03-01T15:00:00Z");

    @Test
    void transacaoNovaViraEntidadeSemId() {
        Transacao nova = Transacao.registrar(1L, TipoTransacao.DESPESA, Dinheiro.de("45"),
                "mercado",AGORA, "gastei 45 no mercado",AGORA);

        TransacaoEntity entidade = TransacaoMapper.paraEntidade(nova);

        assertThat(entidade.getId()).isNull();
        assertThat(entidade.getUsuarioId()).isEqualTo(1L);
        assertThat(entidade.getCategoriaId()).isNull();
        assertThat(entidade.getTipo()).isEqualTo(TipoTransacao.DESPESA);
        assertThat(entidade.getValor()).isEqualTo(new BigDecimal("45.00"));
        assertThat(entidade.getDescricao()).isEqualTo("mercado");
        assertThat(entidade.getOcorridaEm()).isEqualTo(AGORA);
        assertThat(entidade.getMensagemOriginal()).isEqualTo("gastei 45 no mercado");
        assertThat(entidade.getDesfeitaEm()).isNull();
        assertThat(entidade.getCriadaEm()).isEqualTo(AGORA);
    }

    @Test
    void idaEVoltaPreservaTodosOsCampos() {
        Transacao original = Transacao.reconstituir(10L, 1L, 7L, TipoTransacao.RECEITA, Dinheiro.de("3000"),
                "salario",AGORA, "recebi 3000 salário", AGORA.plusSeconds(5),AGORA);

        Transacao volta = TransacaoMapper.paraDominio(TransacaoMapper.paraEntidade(original));

        assertThat(volta).usingRecursiveComparison().isEqualTo(original);
    }


    @Test
    void camposOpcionaisNulosSaoPreservados() {

        TransacaoEntity entidade = new TransacaoEntity(5L, 1L, null, TipoTransacao.DESPESA,
                new BigDecimal("10.00"), null, AGORA, null, null, AGORA);

        Transacao dominio = TransacaoMapper.paraDominio(entidade);

        assertThat(dominio.getId()).isEqualTo(5L);
        assertThat(dominio.getCategoriaId()).isNull();
        assertThat(dominio.getDescricao()).isNull();
        assertThat(dominio.getMensagemOriginal()).isNull();
        assertThat(dominio.ehDesfeita()).isFalse();
    }

    void valorDoBancoViraDinheiroComEscalaDois() {
        TransacaoEntity entidade = new TransacaoEntity(5L, 1L, null, TipoTransacao.DESPESA,
                new BigDecimal("10.5"), "x", AGORA, null, null, AGORA);

        assertThat(TransacaoMapper.paraDominio(entidade).getValor()).isEqualTo(Dinheiro.de("10.50"));
    }



}