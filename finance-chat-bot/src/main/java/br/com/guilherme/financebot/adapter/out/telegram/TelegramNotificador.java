package br.com.guilherme.financebot.adapter.out.telegram;

import br.com.guilherme.financebot.application.port.out.NotificacaoFalhouException;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.domain.valueobject.Usuario;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;


/**
 * Adapter de saída: entrega mensagens pelo método sendMessage da Bot API do Telegram.
 *
 * - Texto puro, sem parse_mode: descrições com "_" ou "*" quebrariam Markdown e o Telegram
 *   recusaria a mensagem inteira.
 * - Em chat privado, chat_id é o próprio id do usuário no Telegram.
 * - Timeouts curtos: um Telegram lento não pode travar o processamento. (O long polling de
 *   getUpdates precisa de outro cliente, com timeout de leitura maior que o do polling.)
 * - O token faz parte da URL da API, então ele é removido de qualquer mensagem de erro.
 */
@Component
public class TelegramNotificador implements NotificadorPort {

    private static final String ENDPOINT_ENVIO = "/sendMessage";
    private static final String TOKEN_OCULTO = "***";
    private static final Duration TIMEOUT_CONEXAO = Duration.ofSeconds(5);
    private static final Duration TIMEOUT_LEITURA = Duration.ofSeconds(10);

    private final RestClient restClient;
    private final String botToken;

    @Autowired
    public TelegramNotificador(RestClient.Builder builder,
                               @Value("telegram.bot-token: ${TELEGRAM_BOT_TOKEN}") String botToken,
                               @Value("${telegram.api-base-url:https://api.telegram.org}") String apiBaseUrl) {
        this(montarRestClient(builder, apiBaseUrl, botToken), botToken);
    }

    /** Usado nos testes, com um RestClient apontado para um servidor falso. */
    TelegramNotificador(RestClient restClient, String botToken) {
        this.restClient = Objects.requireNonNull(restClient, "restClient não pode ser nulo");
        this.botToken = exigirToken(botToken);
    }

    @Override
    public void notificar(Usuario destinatario, String mensagem) {
        Objects.requireNonNull(destinatario, "destinatario não pode ser nulo");
        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("mensagem não pode ser vazia");
        }

        try {
            restClient.post()
                    .uri(ENDPOINT_ENVIO)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("chat_id", destinatario.getTelegramId(), "text", mensagem))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            // O Telegram respondeu com erro (400, 401, 403, 429...). O corpo traz a "description".
            throw new NotificacaoFalhouException("Telegram recusou o envio (HTTP "
                    + e.getStatusCode().value() + "): " + ocultarToken(e.getResponseBodyAsString()));
        } catch (RestClientException e) {
            // Falha de rede/timeout. A mensagem original costuma conter a URL, ou seja, o token.
            throw new NotificacaoFalhouException("Falha de comunicação com o Telegram: "
                    + ocultarToken(e.getMessage()));
        }
    }

    private String ocultarToken(String texto) {
        return texto == null ? "" : texto.replace(botToken, TOKEN_OCULTO);
    }

    private static RestClient montarRestClient(RestClient.Builder builder, String apiBaseUrl, String botToken) {
        String token = exigirToken(botToken);

        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIMEOUT_CONEXAO).build());
        fabrica.setReadTimeout(TIMEOUT_LEITURA);

        return builder
                .baseUrl(apiBaseUrl + "/bot" + token)
                .requestFactory(fabrica)
                .build();
    }

    private static String exigirToken(String botToken) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException(
                    "telegram.bot-token não configurado (defina a variável de ambiente TELEGRAM_BOT_TOKEN)");
        }
        return botToken;
    }
}
