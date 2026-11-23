package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.Transacao;

public final class Respostas {


    static final String AJUDA = """
            Eu registro suas despesas e receitas. Escreva do jeito que você falaria:
            - gastei 45 no mercado
            - uber 23,50
            - recebi 3000 salário
            - paguei 89,90 na farmácia ontem""";

    static final String COMANDO_INDISPONIVEL =
            "Esse comando ainda não está disponível. Por enquanto, envie despesas e receitas, "
                    + "por exemplo: gastei 45 no mercado";

    private Respostas() {
    }

    static String valorSemDescricao(Dinheiro valor) {
        return "Entendi o valor " + valor.formatar() + ", mas faltou a descrição. "
                + "Exemplo: gastei 45 no mercado";
    }

    static String valorAcimaDoLimite() {
        return "Esse valor é alto demais. O máximo que consigo registrar é "
                + Transacao.VALOR_MAXIMO.formatar() + ".";
    }
}
