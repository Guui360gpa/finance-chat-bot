package br.com.guilherme.financebot.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TextosTest {

    @Test
    void textoCurtoNaoMuda() {
        assertThat(Textos.truncar("mercado", 200)).isEqualTo("mercado");
    }

    @Test
    void textoLongoEhCortadoNoLimite() {
        assertThat(Textos.truncar("a".repeat(300), 200)).isEqualTo("a".repeat(200));
    }

    @Test
    void naoPartePedacosDeEmoji() {
        String entrada = "a".repeat(199) + "😀😀"; // 201 caracteres (code points)

        assertThat(Textos.truncar(entrada, 200)).isEqualTo("a".repeat(199) + "😀");
    }

}