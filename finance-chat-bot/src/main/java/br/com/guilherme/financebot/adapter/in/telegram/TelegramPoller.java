package br.com.guilherme.financebot.adapter.in.telegram;

import br.com.guilherme.financebot.adapter.in.telegram.dto.Update;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.telegram", name = "polling-enabled", havingValue = "true",matchIfMissing = true)
public class TelegramPoller {

    private static final Logger log = LoggerFactory.getLogger(TelegramPoller.class);
    private static final int TIMEOUT_LONG_POLLING_SEGUNDOS = 30;

    private final TelegramClient client;
    private volatile boolean rodando = true;
    private long offset = 0;
    private Thread thread;

    public TelegramPoller(TelegramClient client){
        this.client = client;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {
        thread = Thread.ofPlatform()
                .name("telegram-poller")
                .daemon(true)
                .start(this::loop);
        log.info("Long polling do Telegram iniciado");
    }

    @PreDestroy
    public void parar() {
        rodando = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    private void loop() {
        while (rodando) {
            try {
                List<Update> updates = client.getUpdates(offset, TIMEOUT_LONG_POLLING_SEGUNDOS);
                for (Update update : updates) {
                    offset = update.updateId() + 1;
                    if (update.message() != null){
                        client.enviarMensagem(update.message().chat().id(), "pong");
                    }
                }
            }catch (Exception ex) {
                if (!rodando) {
                    break;
                }

                log.warn("Falha no polling: {}", ex.getClass().getSimpleName());
                esperar(5000);
            }

        }
    }

    private void esperar(int milissegundos) {
        try {
            Thread.sleep(milissegundos);
        }catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            rodando = false;
        }
    }


}
