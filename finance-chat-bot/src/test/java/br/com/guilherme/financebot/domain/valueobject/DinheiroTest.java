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



}
