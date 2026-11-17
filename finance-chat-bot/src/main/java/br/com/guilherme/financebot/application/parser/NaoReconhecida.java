package br.com.guilherme.financebot.application.parser;

import java.util.function.Supplier;

/** Nenhum parser da cadeia entendeu a mensagem: o usuário recebe a resposta de ajuda. */

public record NaoReconhecida() implements MensagemInterpretada {
}
