package br.com.guilherme.financebot.application.parser;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Primeiro elo da cadeia. Toda mensagem que começa com "/" é uma tentativa de comando:
 * se for conhecido vira ComandoInterpretado, senão vira NaoReconhecida. Assim "/foo 45"
 * nunca cai no GastoRegexParser e vira uma despesa por engano.
 */

public class ComandoParser implements ParserMensagem{
    @Override
    public Optional<MensagemInterpretada> interpretar(String texto, LocalDate hoje) {
        if (texto == null) {
            return Optional.empty();
        }

        String limpo = texto.strip();
        if (!limpo.startsWith("/")) {
            return Optional.empty();
        }

        String[] partes = limpo.split("\\s+", 2);
        String nome = partes[0].substring(1);
        int arroba = nome.indexOf('@'); // "/hoje@MeuBot" (comportamento do Telegram em grupos)
        if (arroba >= 0) {
            nome = nome.substring(0,arroba);
        }
        String argumentos = partes.length > 1 ? partes[1].strip() : "";

        MensagemInterpretada resultado = Comando.porNome(nome)
                .<MensagemInterpretada>map(comando -> new ComandoInterpretado(comando,argumentos))
                .orElseGet(NaoReconhecida::new);

        return Optional.of(resultado);
    }
}
