package br.com.guilherme.financebot.application.port.in;

import br.com.guilherme.financebot.domain.model.Extrato;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.LocalDate;

/** Porta de entrada: consulta as transações de um dia, no calendário do usuário. */
public interface ConsultarExtratoDoDiaUseCase {

    /** @param data dia no fuso do usuário (ex.: o "hoje" dele) */
    Extrato consultar(Usuario usuario, LocalDate data);
}
