package br.com.guilherme.financebot.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramConfig {

    @Bean
    public RestClient telegramRestClient(TelegramProperties properties) {
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(40));

        return RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + properties.token())
                .requestFactory(factory)
                .build();
    }
}
