package br.com.guilherme.financebot.application.port.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MensagemRecebidaTest {

    @Test
    void nomeEhOpcional() {
        assertThat(new MensagemRecebida(1L, 999L, null, "oi").nome()).isNull();
    }

    @Test
    void textoEhObrigatorio() {
        assertThatThrownBy(() -> new MensagemRecebida(1L, 999L, "G", null))
                .isInstanceOf(NullPointerException.class);
    }

}