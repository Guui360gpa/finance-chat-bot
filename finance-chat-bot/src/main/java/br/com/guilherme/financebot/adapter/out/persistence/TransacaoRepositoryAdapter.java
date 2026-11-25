package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.model.Transacao;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/** Adapter de saída: implementa a porta da aplicação usando JPA. */
@Repository
public class TransacaoRepositoryAdapter implements TransacaoRepository {

    private final TransacaoJpaRepository jpa;

    public TransacaoRepositoryAdapter(TransacaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Transacao> listarNoPeriodo(Long id, Instant inicio, Instant fim) {
        return List.of();
    }

    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoEntity salva = jpa.save(TransacaoMapper.paraEntidade(transacao));
        return TransacaoMapper.paraDominio(salva);
    }
}
