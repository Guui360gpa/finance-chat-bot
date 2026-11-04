package br.com.guilherme.financebot.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;


public class Dinheiro implements Comparable<Dinheiro> {

    private static final int ESCALA = 2;
    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_UP;

    private static final Pattern FORMATO_BR =
            Pattern.compile("^(?:\\d{1,3}(?:\\.\\d{3})+|\\d+)(?:,\\d{1,2})?$");

    private static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    private final BigDecimal valor;

    private Dinheiro(BigDecimal valor) {
        this.valor = valor.setScale(ESCALA,ARREDONDAMENTO);
    }

    public static Dinheiro zero() {
        return ZERO;
    }

    public static Dinheiro de(BigDecimal valor) {
        Objects.requireNonNull(valor, "Valor não pode ser nulo");
        return new Dinheiro(valor);
    }

    public static Dinheiro de(String valor) {
        Objects.requireNonNull(valor, "Valor não pode ser nulo");
        try {
            return new Dinheiro(new BigDecimal(valor));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor monetário inválido: '" + valor + "'" ,ex);
        }
    }

    public static Dinheiro deTextoBr(String texto) {
        Objects.requireNonNull(texto, "texto não pode ser nulo");

        String limpo = texto
                .replaceAll("(?i)R\\$", "")
                .replaceAll("\\s+", "");

        if (!FORMATO_BR.matcher(limpo).matches()) {
            throw new IllegalArgumentException("Valor monetário inválido: '" + texto + "'");
        }

        String normalizado = limpo.replace(".","").replace(",",".");
        return de(normalizado);
    }

    public BigDecimal valor() {
        return valor;
    }

    public Dinheiro somar(Dinheiro outro) {
        Objects.requireNonNull(outro, "outro não pode ser nulo");
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        Objects.requireNonNull(outro, "outro não pode ser nulo");
        return new Dinheiro(valor.subtract(outro.valor));
    }

    public boolean ehMaiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    public boolean ehMenorQue(Dinheiro outro) {
        return compareTo(outro) < 0;
    }

    public boolean ehZero() {
        return valor.signum() == 0;
    }

    public boolean ehPositivo() {
        return valor.signum() > 0;
    }

    public boolean ehNegativo() {
        return valor.signum() < 0;
    }


    @Override
    public int compareTo(Dinheiro outro) {
        Objects.requireNonNull(outro, "outro não pode ser nulo");
        return valor.compareTo(outro.valor);
    }

    public String formatar() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        DecimalFormat formato = new DecimalFormat("#,##0.00",simbolos);
        String numero = formato.format(valor.abs());

        return (ehNegativo() ? "-R$ " : "R$ ") + numero;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Dinheiro outro)) return false;

        return valor.equals(outro.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return formatar();
    }
}
