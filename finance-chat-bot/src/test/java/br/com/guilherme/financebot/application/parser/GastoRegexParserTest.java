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

    // 1º de março de propósito: "ontem" cai em 28/02, então o teste cobre a virada de mês.
    private static final LocalDate HOJE = LocalDate.of(2026, 3, 1);

    private final GastoRegexParser parser = new GastoRegexParser();

    @DisplayName("Casos da seção 7 do documento")
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
        assertThat(parser.interpretar(entrada, HOJE))
                .contains(new GastoInterpretado(tipo, Dinheiro.de(valor), descricao, HOJE.minusDays(diasAtras)));
    }

    @DisplayName("Variações de escrita")
    @ParameterizedTest(name = "\"{0}\" -> {1} {2} \"{3}\" ({4} dia(s) atrás)")
    @CsvSource(delimiter = '|', value = {
            "Gastei 45 no Mercado              | DESPESA | 45.00   | Mercado  | 0",
            "paguei R$89,90 na farmácia        | DESPESA | 89.90   | farmácia | 0",
            "mercado 45 ontem                  | DESPESA | 45.00   | mercado  | 1",
            "comprei 1.234,56 notebook         | DESPESA | 1234.56 | notebook | 0",
            "ganhei 150 freela hoje            | RECEITA | 150.00  | freela   | 0"
    })
    void interpretaVariacoes(String entrada, TipoTransacao tipo, String valor,
                             String descricao, int diasAtras) {
        assertThat(parser.interpretar(entrada, HOJE))
                .contains(new GastoInterpretado(tipo, Dinheiro.de(valor), descricao, HOJE.minusDays(diasAtras)));
    }

    @DisplayName("Valor sem descrição: rejeitada com aviso (MensagemIncompleta)")
    @ParameterizedTest(name = "\"{0}\" -> incompleta com {1}")
    @CsvSource(delimiter = '|', value = {
            "gastei 45        | 45.00",
            "45               | 45.00",
            "R$ 23,50         | 23.50",
            "gastei 45 no     | 45.00",
            "gastei 45 hoje   | 45.00"
    })
    void rejeitaValorSemDescricao(String entrada, String valor) {
        assertThat(parser.interpretar(entrada, HOJE))
                .contains(new MensagemIncompleta(Dinheiro.de(valor)));
    }

    @DisplayName("Mensagens que não são gasto: o parser devolve vazio")
    @ParameterizedTest(name = "\"{0}\" -> vazio")
    @ValueSource(strings = {
            "bom dia", "oi", "tudo bem?", "gastei muito",
            "gastei 0 no mercado",     // valor zero
            "gastei 1.5 no mercado",   // "1.5" não é decimal em pt-BR: melhor recusar do que gravar errado
            "/hoje"                    // comando não é gasto
    })
    void naoReconhece(String entrada) {
        assertThat(parser.interpretar(entrada, HOJE)).isEmpty();
    }
}