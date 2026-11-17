package br.com.guilherme.financebot.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrcamentoTest {

    @Test
    void criaOrcamentoValido() {
        Orcamento o = Orcamento.novo(1L,2L,Dinheiro.de("800"));

        assertThat(o.getId()).isNull();
        assertThat(o.getUsuarioId()).isEqualTo(1L);
        assertThat(o.getCategoriaId()).isEqualTo(2L);
        assertThat(o.getLimite()).isEqualTo(Dinheiro.de("800"));
    }

    @Test
    void rejeitaLimiteZeroOuNegativo() {

        //rejeita limite zero
        assertThatThrownBy(() -> Orcamento.novo(1L,2L,Dinheiro.zero()))
                .isInstanceOf(IllegalArgumentException.class);

        //rejeita limite negativo
        assertThatThrownBy(() -> Orcamento.novo(1L,2L,Dinheiro.de("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaNulos() {

        // rejeita usuarioId nulo
        assertThatThrownBy(() -> Orcamento.novo(null,2L,Dinheiro.de("800")))
                .isInstanceOf(NullPointerException.class);

        // rejeita categoriaId nulo
        assertThatThrownBy(() -> Orcamento.novo(1L,null,Dinheiro.de("800")))
                .isInstanceOf(NullPointerException.class);

        // rejeita limite nulo
        assertThatThrownBy(() -> Orcamento.novo(1L,2L,null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void alterarLimiteValido() {
        Orcamento o = Orcamento.novo(1L,2L,Dinheiro.de("800"));

        o.alterarLimite(Dinheiro.de("1000"));
        assertThat(o.getLimite()).isEqualTo(Dinheiro.de("1000"));
    }

    @Test
    void rejeitaAlteracaoLimiteNegativoOuNulo() {
        Orcamento o = Orcamento.novo(1L,2L,Dinheiro.de("800"));

        // rejeita alteração para valor nulo
        assertThatThrownBy(() -> o.alterarLimite(null))
                .isInstanceOf(NullPointerException.class);

        // rejeita alteração para valor negativo
        assertThatThrownBy(() -> o.alterarLimite(Dinheiro.de("-5")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
