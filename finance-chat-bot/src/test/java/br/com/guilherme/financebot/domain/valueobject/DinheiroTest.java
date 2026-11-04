package br.com.guilherme.financebot.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class DinheiroTest {

    @Nested
    @DisplayName("Criação")
    public class Criacao{

        @Test
        void criaAPartirDeString() {
            assertThat(Dinheiro.de("23.50").valor()).isEqualByComparingTo("23.50");
        }

        @Test
        void criaAPartirDeBigDecimal() {
            assertThat(Dinheiro.de(new BigDecimal("45")).valor()).isEqualByComparingTo("45.00");
        }

        @Test
        void semprePossuiEscalaDois() {
            assertThat(Dinheiro.de("45").valor().scale()).isEqualTo(2);
            assertThat(Dinheiro.de("45.5").valor().scale()).isEqualTo(2);
            assertThat(Dinheiro.zero().valor().scale()).isEqualTo(2);
        }

        @Test
        void zeroEhZero() {
            assertThat(Dinheiro.zero().ehZero()).isTrue();
            assertThat(Dinheiro.zero()).isEqualTo(Dinheiro.de("0.00"));
        }

        @Test
        void rejeitaNulo() {
            assertThatThrownBy(() -> Dinheiro.de((String) null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> Dinheiro.de((BigDecimal) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "abc", "12,50", "1.2.3"})
        void rejeitaStringInvalida(String entrada) {
            assertThatThrownBy(() -> Dinheiro.de(entrada))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Igualdade {

        @Test
        void escalasDiferentesSaoIguais() {
            assertThat(Dinheiro.de("45")).isEqualTo(Dinheiro.de("45.00"));
            assertThat(Dinheiro.de("45").hashCode()).isEqualTo(Dinheiro.de("45.00").hashCode());
        }

        @Test
        void valoresDiferentesNaoSaoIguais() {
            assertThat(Dinheiro.de("45.00")).isNotEqualTo(Dinheiro.de("45.01"));
        }
    }

    @Nested
    @DisplayName("Aritmética")
    class Aritmetica {

        @Test
        void soma() {
            assertThat(Dinheiro.de("45.00").somar(Dinheiro.de("23.50")))
                    .isEqualTo(Dinheiro.de("68.50"));
        }

        @Test
        void somaNaoTemErroDePontoFlutuante() {
            // com double, 0.1 + 0.2 = 0.30000000000000004
            assertThat(Dinheiro.de("0.10").somar(Dinheiro.de("0.20")))
                    .isEqualTo(Dinheiro.de("0.30"));
        }

        @Test
        void subtracao() {
            assertThat(Dinheiro.de("100.00").subtrair(Dinheiro.de("45.50")))
                    .isEqualTo(Dinheiro.de("54.50"));
        }

        @Test
        void subtracaoPodeResultarEmNegativo() {
            Dinheiro saldo = Dinheiro.de("50.00").subtrair(Dinheiro.de("80.00"));

            assertThat(saldo).isEqualTo(Dinheiro.de("-30.00"));
            assertThat(saldo.ehNegativo()).isTrue();
        }

        @Test
        void operacoesNaoAlteramOOriginal() {
            Dinheiro original = Dinheiro.de("10.00");
            original.somar(Dinheiro.de("5.00"));

            assertThat(original).isEqualTo(Dinheiro.de("10.00"));
        }

        @Test
        void somaRejeitaNulo() {
            assertThatThrownBy(() -> Dinheiro.de("1").somar(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Comparação")
    class Comparacao {

        @Test
        void maiorEMenor() {
            Dinheiro cinco = Dinheiro.de("5.00");
            Dinheiro dez = Dinheiro.de("10.00");

            assertThat(dez.ehMaiorQue(cinco)).isTrue();
            assertThat(cinco.ehMaiorQue(dez)).isFalse();
            assertThat(cinco.ehMenorQue(dez)).isTrue();
            assertThat(dez.ehMenorQue(cinco)).isFalse();
        }

        @Test
        void valoresIguaisNaoSaoMaioresNemMenores() {
            Dinheiro a = Dinheiro.de("10.00");
            Dinheiro b = Dinheiro.de("10");

            assertThat(a.ehMaiorQue(b)).isFalse();
            assertThat(a.ehMenorQue(b)).isFalse();
            assertThat(a.compareTo(b)).isZero();
        }

        @Test
        void sinais() {
            assertThat(Dinheiro.de("1.00").ehPositivo()).isTrue();
            assertThat(Dinheiro.de("-1.00").ehNegativo()).isTrue();
            assertThat(Dinheiro.zero().ehPositivo()).isFalse();
            assertThat(Dinheiro.zero().ehNegativo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Arredondamento (HALF_UP, 2 casas)")
    class Arredondamento {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({
                "10.004, 10.00",
                "10.005, 10.01",
                "10.015, 10.02",
                "10.994, 10.99",
                "10.995, 11.00",
                "-10.005, -10.01"
        })
        void arredondaParaDuasCasas(String entrada, String esperado) {
            assertThat(Dinheiro.de(entrada)).isEqualTo(Dinheiro.de(esperado));
        }
    }
}
