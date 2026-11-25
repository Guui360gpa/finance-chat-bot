package br.com.guilherme.financebot.testsupport;


import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.model.Transacao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/** Fake de TransacaoRepository: guarda em memória e atribui ids como o banco faria. */
public class TransacaoRepositoryEmMemoria implements TransacaoRepository {

    private final List<Transacao> salvas = new ArrayList<>();
    private final AtomicLong sequencia = new AtomicLong();


    @Override
    public Transacao salvar(Transacao t) {
        Transacao salva = Transacao.reconstituir(sequencia.incrementAndGet(),t.getUsuarioId(),t.getCategoriaId(),
                t.getTipo(),t.getValor(),t.getDescricao(),t.getOcorridaEm(),t.getMensagemOriginal(),
                t.getDesfeitaEm(),t.getCriadaEm());
        salvas.add(salva);
        return salva;
    }

    @Override
    public List<Transacao> listarNoPeriodo(Long id, Instant inicio, Instant fim) {
        return List.of();
    }

    public List<Transacao> todas() {
        return List.copyOf(salvas);
    }
}
