package br.com.guilherme.financebot.application.parser;

import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interpreta texto livre como despesa ou receita.
 *
 * Estratégia "extrair e subtrair": identifica o verbo inicial, a data relativa e o valor,
 * remove esses trechos do texto e o que sobra (sem conectivos como "no", "na") é a descrição.
 *
 * Regras:
 * - sem verbo, o tipo padrão é DESPESA; receita só com verbo explícito (recebi, ganhei);
 * - sem descrição, devolve MensagemIncompleta;
 * - valor inválido ("1.5"), zero ou ausente, devolve vazio (não é comigo).
 */

public class GastoRegexParser implements ParserMensagem{


    private static final Map<String, TipoTransacao> VERBOS = Map.of(
            "gastei", TipoTransacao.DESPESA,
            "paguei", TipoTransacao.DESPESA,
            "comprei", TipoTransacao.DESPESA,
            "recebi", TipoTransacao.RECEITA,
            "ganhei", TipoTransacao.RECEITA);

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    /** Verbo no início do texto, como palavra inteira. */
    private static final Pattern VERBO_INICIAL = Pattern.compile(
            "^(" + String.join("|", VERBOS.keySet()) + ")(?!\\p{L})", FLAGS);

    /** "ontem" ou "hoje" como palavra inteira (não casa dentro de "anteontem"). */
    private static final Pattern DATA_RELATIVA = Pattern.compile(
            "(?<!\\p{L})(ontem|hoje)(?!\\p{L})", FLAGS);

    /**
     * Valor: "R$" opcional e um número que começa e termina em dígito, podendo ter . e , no meio.
     * Os lookarounds impedem casar número colado em letra/dígito ("uber23", "45kg").
     * A validação fina do formato (milhar, decimais) fica com Dinheiro.deTextoBr.
     */
    private static final Pattern VALOR = Pattern.compile(
            "(?<![\\p{L}\\p{N}.,])(?:R\\$\\s*)?(\\d(?:[\\d.,]*\\d)?)(?![\\p{L}\\p{N}])", FLAGS);

    private static final Pattern CONECTIVO_INICIAL = Pattern.compile(
            "^(?:no|na|nos|nas|em|de|do|da|com|pra|para|num|numa)(?:\\s+|$)", FLAGS);

    private static final Pattern PONTUACAO_FINAL = Pattern.compile("[\\s.,;:!?]+$");

    @Override
    public Optional<MensagemInterpretada> interpretar(String texto, LocalDate hoje) {
        Objects.requireNonNull(hoje, "hoje não pode ser nulo");
        if (texto == null || texto.isBlank() || texto.stripLeading().startsWith("/")) {
            return Optional.empty();
        }

        String restante = texto.strip();

        TipoTransacao tipo = TipoTransacao.DESPESA;
        Matcher verbo = VERBO_INICIAL.matcher(restante);
        if (verbo.find()) {
            tipo = VERBOS.get(verbo.group(1).toLowerCase(Locale.ROOT));
            restante = restante.substring(verbo.end());
        }

        LocalDate data = hoje;
        Matcher dataRelativa = DATA_RELATIVA.matcher(restante);
        if (dataRelativa.find()) {
            if (dataRelativa.group(1).equalsIgnoreCase("ontem")) {
                data = hoje.minusDays(1);
            }
            restante = removerTrecho(restante, dataRelativa);
        }

        Matcher trechoValor = VALOR.matcher(restante);
        if (!trechoValor.find()) {
            return Optional.empty();
        }
        Dinheiro valor;
        try {
            valor = Dinheiro.deTextoBr(trechoValor.group(1));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (!valor.ehPositivo()) {
            return Optional.empty();
        }
        restante = removerTrecho(restante, trechoValor);

        String descricao = limparDescricao(restante);
        if (descricao.isEmpty()) {
            return Optional.of(new MensagemIncompleta(valor));
        }
        return Optional.of(new GastoInterpretado(tipo, valor, descricao, data));
    }

    private static String removerTrecho(String texto, Matcher trecho) {
        return texto.substring(0, trecho.start()) + " " + texto.substring(trecho.end());
    }

    private static String limparDescricao(String texto) {
        String descricao = texto.replaceAll("\\s+", " ").strip();
        descricao = PONTUACAO_FINAL.matcher(descricao).replaceFirst("").strip();
        descricao = CONECTIVO_INICIAL.matcher(descricao).replaceFirst("").strip();
        return descricao;
    }
}
