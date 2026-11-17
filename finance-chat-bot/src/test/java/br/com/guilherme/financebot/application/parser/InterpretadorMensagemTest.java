package br.com.guilherme.financebot.application.parser;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InterpretadorMensagemTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 3, 1);

    @Nested
    @DisplayName("Mecânica da cadeia (parsers falsos)")
    class Mecanica {

        private final ParserMensagem naoEntende = (texto, hoje) -> Optional.empty();
        private final ParserMensagem entendeComo1 =
                (texto, hoje) -> Optional.of(new MensagemIncompleta(Dinheiro.de("1")));
        private final ParserMensagem entendeComo2 =
                (texto, hoje) -> Optional.of(new MensagemIncompleta(Dinheiro.de("2")));

        @Test
        void oPrimeiroParserQueReconheceGanha() {
            var interpretador = new InterpretadorMensagem(List.of(naoEntende, entendeComo1, entendeComo2));

            assertThat(interpretador.interpretar("qualquer coisa", HOJE))
                    .isEqualTo(new MensagemIncompleta(Dinheiro.de("1")));
        }

        @Test
        void semNenhumReconhecimentoDevolveNaoReconhecida() {
            var interpretador = new InterpretadorMensagem(List.of(naoEntende));

            assertThat(interpretador.interpretar("qualquer coisa", HOJE)).isEqualTo(new NaoReconhecida());
        }

        @Test
        void textoNuloOuVazioNemChegaAosParsers() {
            var interpretador = new InterpretadorMensagem(List.of(entendeComo1));

            assertThat(interpretador.interpretar(null, HOJE)).isEqualTo(new NaoReconhecida());
            assertThat(interpretador.interpretar("   ", HOJE)).isEqualTo(new NaoReconhecida());
        }
    }

    @Nested
    @DisplayName("Cadeia real (ComandoParser, depois GastoRegexParser)")
    class CadeiaReal {

        private final InterpretadorMensagem interpretador =
                new InterpretadorMensagem(List.of(new ComandoParser(), new GastoRegexParser()));

        @Test
        void comandoEhInterpretadoComoComando() {
            assertThat(interpretador.interpretar("/hoje", HOJE))
                    .isEqualTo(new ComandoInterpretado(Comando.HOJE, ""));
        }

        @Test
        void gastoEhInterpretadoComoGasto() {
            assertThat(interpretador.interpretar("gastei 45 no mercado", HOJE))
                    .isEqualTo(new GastoInterpretado(TipoTransacao.DESPESA, Dinheiro.de("45"), "mercado", HOJE));
        }

        @Test
        void valorSemDescricaoViraMensagemIncompleta() {
            assertThat(interpretador.interpretar("gastei 45", HOJE))
                    .isEqualTo(new MensagemIncompleta(Dinheiro.de("45")));
        }

        @Test
        void saudacaoViraNaoReconhecida() {
            assertThat(interpretador.interpretar("bom dia", HOJE)).isEqualTo(new NaoReconhecida());
        }

        @Test
        void comandoDesconhecidoComNumeroNaoViraDespesa() {
            assertThat(interpretador.interpretar("/foo 45", HOJE)).isEqualTo(new NaoReconhecida());
        }
    }

}