package br.com.guilherme.financebot.adapter.in.telegram.dto;

public record TelegramResponse<T>(
        boolean ok,
        T result
) {
}
