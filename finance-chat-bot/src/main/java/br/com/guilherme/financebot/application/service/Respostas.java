package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.domain.model.Extrato;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class Respostas {


    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    /** Resposta do /hoje: lista do dia e totais. */
    static String extratoDoDia(LocalDate data, Extrato extrato) {
        String dia = data.format(DATA_BR);
        if (extrato.estaVazio()) {
            return "Nenhuma transação registrada hoje (" + dia + ").";
        }

        StringBuilder texto = new StringBuilder("Hoje (" + dia + "):");
        for (Transacao transacao : extrato.transacoes()) {
            texto.append("\n- ")
                    .append(transacao.ehDespesa() ? "Despesa" : "Receita")
                    .append(": ").append(transacao.getValor().formatar());
            if (transacao.getDescricao() != null) {
                texto.append(" (").append(transacao.getDescricao()).append(")");
            }
        }
        texto.append("\n\nReceitas: ").append(extrato.totalReceitas().formatar())
                .append("\nDespesas: ").append(extrato.totalDespesas().formatar())
                .append("\nSaldo: ").append(extrato.saldo().formatar());
        return texto.toString();
    }
}
