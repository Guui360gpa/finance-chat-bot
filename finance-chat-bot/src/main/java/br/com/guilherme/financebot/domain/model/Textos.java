package br.com.guilherme.financebot.domain.model;


/** Utilidades de texto do domínio. */
final class Textos {

    private Textos() {
    }

    /**
     * Corta o texto em no máximo N caracteres (code points, como o VARCHAR do PostgreSQL conta),
     * sem partir um emoji ao meio.
     */
    static String truncar(String texto, int maximoDeCaracteres) {
        if (texto.codePointCount(0, texto.length()) <= maximoDeCaracteres) {
            return texto;
        }
        return texto.substring(0, texto.offsetByCodePoints(0, maximoDeCaracteres));
    }
}
