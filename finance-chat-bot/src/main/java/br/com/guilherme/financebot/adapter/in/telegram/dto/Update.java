package br.com.guilherme.financebot.adapter.in.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Update(
        @JsonProperty("update_id") long updateId,
        Message message
) {
}
