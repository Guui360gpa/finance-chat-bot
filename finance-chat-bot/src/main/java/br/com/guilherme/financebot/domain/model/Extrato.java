package br.com.guilherme.financebot.domain.model;


import br.com.guilherme.financebot.domain.valueobject.Dinheiro;

import java.util.Comparator;
import java.util.List;


/**
 * Conjunto de transações de um período, com os totais.
 * Regra de negócio: transações desfeitas NÃO contam. Quem consulta o banco pode devolver tudo;
 * o filtro mora aqui, uma vez só, e vale também para o /resumo da Etapa 4.
 * As transações ficam ordenadas da mais antiga para a mais recente.
 */
public record Extrato(List<Transacao> transacoes) {

    /**
     * Construtor compacto: roda sempre, seja qual for o jeito de criar o Extrato. (Um construtor
     * secundário com Collection seria ignorado quando se passa uma List, porque o canônico é
     * mais específico.)
     */
    public Extrato {
        transacoes = transacoes.stream()
                .filter(t -> !t.ehDesfeita())
                .sorted(Comparator.comparing(Transacao::getOcorridaEm))
                .toList();
    }

    public boolean estaVazio() {
        return transacoes.isEmpty();
    }

    public Dinheiro totalReceitas() {
        return somar(true);
    }

    public Dinheiro totalDespesas() {
        return somar(false);
    }

    public Dinheiro saldo() {
        return totalReceitas().subtrair(totalDespesas());
    }

    private Dinheiro somar(boolean receitas){
        return transacoes.stream()
                .filter(t -> t.ehReceita() == receitas)
                .map(Transacao::getValor)
                .reduce(Dinheiro.zero(), Dinheiro::somar);
    }
}
