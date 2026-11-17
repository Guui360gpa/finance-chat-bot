package br.com.guilherme.financebot.application.parser;

import java.time.LocalDate;
import java.util.Optional;

public class GastoRegexParser implements ParserMensagem{


    @Override
    public Optional<MensagemInterpretada> interpretar(String texto, LocalDate hoje) {
        return Optional.empty();
    }
}
