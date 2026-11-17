package br.com.guilherme.financebot.application.parser;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class GastoRegexParserTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 3, 1);

    private final GastoRegexParser parser = new GastoRegexParser();

    @DisplayName("Mensagens de despesa e receita reconhecidas")
    @ParameterizedTest(name = "\"{0}\" -> {1} {2} \"{3}\" ({4} dia(s) atrás)")
    @CsvSource(delimiter = '|', value = {
            "gastei 45 no mercado              | DESPESA | 45.00   | mercado  | 0",
            "uber 23,50                        | DESPESA | 23.50   | uber     | 0",
            "R$ 1.200 aluguel                  | DESPESA | 1200.00 | aluguel  | 0",
            "recebi 3000 salário               | RECEITA | 3000.00 | salário  | 0",
            "paguei 89,90 na farmácia ontem    | DESPESA | 89.90   | farmácia | 1"
    })
    void interpretaGastoOuReceita(String entrada, TipoTransacao tipo, String valor,
                                  String descricao, int diasAtras) {
        GastoInterpretado esperado =
                new GastoInterpretado(tipo, Dinheiro.de(valor), descricao, HOJE.minusDays(diasAtras));

        assertThat(parser.interpretar(entrada, HOJE)).contains(esperado);
    }

    @DisplayName("Mensagens sem valor não são reconhecidas")
    @ParameterizedTest(name = "\"{0}\" -> vazio")
    @ValueSource(strings = {"bom dia", "oi", "tudo bem?"})
    void naoReconheceMensagemSemValor(String entrada) {
        assertThat(parser.interpretar(entrada, HOJE)).isEmpty();
    }

}