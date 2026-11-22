package br.com.guilherme.financebot.adapter.out.telegram;

import br.com.guilherme.financebot.application.port.out.NotificacaoFalhouException;
import br.com.guilherme.financebot.domain.valueobject.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class TelegramNotificadorTest {

    private static final String TOKEN = "123456:ABC-segredo";
    private static final String BASE = "https://api.telegram.org/bot" + TOKEN;

    private final Usuario usuario = Usuario.reconstituir(1L, 999L, "Guilherme",
            ZoneId.of("America/Sao_Paulo"), Instant.parse("2026-01-01T00:00:00Z"));

    private MockRestServiceServer servidor;
    private TelegramNotificador notificador;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        notificador = new TelegramNotificador(builder.build(), TOKEN);
    }

    @Test
    void enviaSendMessageComChatIdETexto() {
        servidor.expect(requestTo(BASE + "/sendMessage"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"chat_id\":999,\"text\":\"Despesa registrada: R$ 45,00\"}"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        notificador.notificar(usuario, "Despesa registrada: R$ 45,00");

        servidor.verify();
    }

    @Test
    void traduzRecusaDoTelegramParaNotificacaoFalhouException() {
        servidor.expect(requestTo(BASE + "/sendMessage"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":false,\"error_code\":403,\"description\":\"Forbidden: bot was blocked by the user\"}"));

        assertThatThrownBy(() -> notificador.notificar(usuario, "oi"))
                .isInstanceOf(NotificacaoFalhouException.class)
                .hasMessageContaining("403")
                .hasMessageContaining("bot was blocked by the user");
    }

    @Test
    void falhaDeRedeNaoVazaOTokenNaMensagem() {
        servidor.expect(requestTo(BASE + "/sendMessage"))
                .andRespond(withException(new IOException("conexão recusada")));

        assertThatThrownBy(() -> notificador.notificar(usuario, "oi"))
                .isInstanceOf(NotificacaoFalhouException.class)
                .hasMessageContaining("Falha de comunicação")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(TOKEN));
    }

    @Test
    void erroQueRepeteOTokenNoCorpoTambemEhOcultado() {
        servidor.expect(requestTo(BASE + "/sendMessage"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":false,\"description\":\"Unauthorized bot" + TOKEN + "\"}"));

        assertThatThrownBy(() -> notificador.notificar(usuario, "oi"))
                .isInstanceOf(NotificacaoFalhouException.class)
                .hasMessageContaining("401")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(TOKEN));
    }

    @Test
    void naoEnviaMensagemVazia() {
        assertThatThrownBy(() -> notificador.notificar(usuario, "  "))
                .isInstanceOf(IllegalArgumentException.class);
        servidor.verify(); // nenhuma requisição foi feita
    }

    @Test
    void falhaNaSubidaSeOTokenNaoEstiverConfigurado() {
        assertThatThrownBy(() -> new TelegramNotificador(RestClient.create(), " "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TELEGRAM_BOT_TOKEN");
    }

}