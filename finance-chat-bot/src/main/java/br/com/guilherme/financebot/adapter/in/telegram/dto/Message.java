package br.com.guilherme.financebot.adapter.in.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
        @JsonProperty("message_id") long messageId,
        Chat chat,
        String text
) {
}
