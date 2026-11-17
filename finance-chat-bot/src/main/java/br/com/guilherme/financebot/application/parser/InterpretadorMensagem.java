package br.com.guilherme.financebot.application.parser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Chain of Responsibility: oferece a mensagem a cada parser, na ordem, e o primeiro que a
 * reconhece ganha. Se nenhum reconhecer, devolve NaoReconhecida.
 * Para incluir um LlmParser no futuro basta acrescentá-lo ao fim da lista (no config).
 */

public class InterpretadorMensagem {

    private final List<ParserMensagem> parsers;

    public InterpretadorMensagem(List<ParserMensagem> parsers) {
        this.parsers = List.copyOf(parsers);
    }

    public MensagemInterpretada interpretar(String texto, LocalDate hoje) {
        if (texto == null || texto.isBlank()) {
            return new NaoReconhecida();
        }
        for (ParserMensagem parser : parsers) {
            Optional<MensagemInterpretada> resultado = parser.interpretar(texto, hoje);
            if (resultado.isPresent()) {
                return resultado.get();
            }
        }
        return new NaoReconhecida();
    }
}
