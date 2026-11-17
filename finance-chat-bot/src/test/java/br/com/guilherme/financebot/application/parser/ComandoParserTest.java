package br.com.guilherme.financebot.application.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ComandoParserTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 3, 1);

    private final ComandoParser parser = new ComandoParser();

    @ParameterizedTest(name = "\"{0}\" -> {1} com argumentos [{2}]")
    @CsvSource(delimiter = '|', value = {
            "/hoje                       | HOJE       | ''",
            "/resumo                     | RESUMO     | ''",
            "/desfazer                   | DESFAZER   | ''",
            "/categorias                 | CATEGORIAS | ''",
            "/orcamento alimentação 800  | ORCAMENTO  | alimentação 800",
            "/HOJE                       | HOJE       | ''",
            "/hoje@FinanceChatBot        | HOJE       | ''",
            "/start                      | AJUDA      | ''"
    })
    void reconheceComandos(String entrada, Comando comando, String argumentos) {
        assertThat(parser.interpretar(entrada, HOJE))
                .contains(new ComandoInterpretado(comando, argumentos));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/foo", "/foo 45"})
    void comandoDesconhecidoEncerraACadeia(String entrada) {
        assertThat(parser.interpretar(entrada, HOJE)).contains(new NaoReconhecida());
    }

    @ParameterizedTest
    @ValueSource(strings = {"gastei 45 no mercado", "bom dia", ""})
    void ignoraMensagensQueNaoSaoComando(String entrada) {
        assertThat(parser.interpretar(entrada, HOJE)).isEmpty();
    }

    @Test
    void ignoraTextoNulo() {
        assertThat(parser.interpretar(null, HOJE)).isEmpty();
    }

}