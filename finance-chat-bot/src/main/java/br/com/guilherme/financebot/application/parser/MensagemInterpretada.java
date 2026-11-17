package br.com.guilherme.financebot.application.parser;

/**
 * Resultado possível de interpretar uma mensagem do usuário.
 * Selada: o compilador conhece todos os casos, então um switch futuro pode ser exaustivo.
 */

public sealed interface MensagemInterpretada permits
        GastoInterpretado,
        ComandoInterpretado,
        MensagemIncompleta,
        NaoReconhecida{
}
