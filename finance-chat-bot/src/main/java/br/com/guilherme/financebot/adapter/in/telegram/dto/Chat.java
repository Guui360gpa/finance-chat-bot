package br.com.guilherme.financebot.adapter.in.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Chat(
        long id
) {
}
