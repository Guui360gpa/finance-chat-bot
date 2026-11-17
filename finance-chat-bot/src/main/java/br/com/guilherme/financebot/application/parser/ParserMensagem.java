package br.com.guilherme.financebot.application.parser;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Elo da Chain of Responsibility: tenta interpretar o texto.
 * Devolve vazio quando a mensagem não é dele, e o próximo elo da cadeia tenta.
 *
 * @param hoje data de hoje no fuso do usuário (necessária para resolver "ontem").
 *             Recebida por parâmetro para o parser continuar puro e testável.
 */

public interface ParserMensagem {

    Optional<MensagemInterpretada> interpretar(String texto, LocalDate hoje);
}
