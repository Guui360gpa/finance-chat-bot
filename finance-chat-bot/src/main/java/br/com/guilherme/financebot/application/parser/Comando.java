package br.com.guilherme.financebot.application.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Comandos que o bot entende. O Telegram manda /start no primeiro contato, por isso AJUDA o atende. */

public enum Comando {

    HOJE("hoje"),
    RESUMO("resumo"),
    DESFAZER("desfazer"),
    CATEGORIAS("categorias"),
    ORCAMENTO("orcamento"),
    AJUDA("ajuda","start","help");

    private final List<String> nomes;

    Comando(String... nomes) {
        this.nomes = List.of(nomes);
    }

    public static Optional<Comando> porNome(String nome) {
        String normalizado = nome.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(comando -> comando.nomes.contains(normalizado))
                .findFirst();
    }
}
