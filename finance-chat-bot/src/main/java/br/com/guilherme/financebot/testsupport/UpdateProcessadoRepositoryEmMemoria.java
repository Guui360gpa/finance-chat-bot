package br.com.guilherme.financebot.testsupport;


import br.com.guilherme.financebot.application.port.out.UpdateProcessadoRepository;

import java.util.LinkedHashSet;
import java.util.Set;

/** Fake de UpdateProcessadoRepository: Set.add já tem a semântica de "registrar se novo". */
public class UpdateProcessadoRepositoryEmMemoria implements UpdateProcessadoRepository {

    private final Set<Long> ids = new LinkedHashSet<>();


    @Override
    public boolean registrarSeNovo(long updateId) {
        return ids.add(updateId);
    }

    public boolean contem(long updateId) {
        return ids.contains(updateId);
    }
}
