package br.com.guilherme.financebot.application.parser;

import java.time.LocalDate;
import java.util.Optional;

public interface ParserMensagem {

    Optional<MensagemInterpretada> interpretar(String texto, LocalDate hoje);
}
