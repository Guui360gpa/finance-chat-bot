package br.com.guilherme.financebot.adapter.in.telegram;

import br.com.guilherme.financebot.adapter.in.telegram.dto.TelegramResponse;
import br.com.guilherme.financebot.adapter.in.telegram.dto.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

@Component
public class TelegramClient {

    private static final ParameterizedTypeReference<TelegramResponse<List<Update>>> TIPO_UPDATES =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public TelegramClient(RestClient telegramRestClient){
        this.restClient = telegramRestClient;
    }

    public List<Update> getUpdates(long offset, int timeoutSegundos) {
        TelegramResponse<List<Update>> resposta = restClient.get()
                .uri(uri -> uri.path("/getUpdates")
                        .queryParam("offset",offset)
                        .queryParam("timeout", timeoutSegundos)
                        .build())
                .retrieve()
                .body(TIPO_UPDATES);

        if (resposta == null || !resposta.ok() || resposta.result() == null) {
            return List.of();
        }
        return resposta.result();
    }

    public void enviarMensagem(long chatId, String texto) {
        restClient.post()
                .uri("/sendMessage")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("chat_id",chatId,"text",texto))
                .retrieve()
                .toBodilessEntity();
    }


}
