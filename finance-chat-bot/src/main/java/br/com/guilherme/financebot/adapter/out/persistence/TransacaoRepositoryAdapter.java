package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.valueobject.Transacao;
import org.springframework.stereotype.Repository;

/** Adapter de saída: implementa a porta da aplicação usando JPA. */
@Repository
public class TransacaoRepositoryAdapter implements TransacaoRepository {

    private final TransacaoJpaRepository jpa;

    public TransacaoRepositoryAdapter(TransacaoJpaRepository jpa) {
        this.jpa = jpa;
    }


    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoEntity salva = jpa.save(TransacaoMapper.paraEntidade(transacao));
        return TransacaoMapper.paraDominio(salva);
    }
}
