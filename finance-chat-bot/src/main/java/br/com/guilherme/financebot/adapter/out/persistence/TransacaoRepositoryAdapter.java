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

    TransacaoRepositoryAdapter(TransacaoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transacao salvar(Transacao transacao) {
        TransacaoEntity salva = jpa.save(TransacaoMapper.paraEntidade(transacao));
        return TransacaoMapper.paraDominio(salva);
    }

    @Override
    public List<Transacao> listarNoPeriodo(Long usuarioId, Instant inicio, Instant fim) {
        return jpa.buscarNoPeriodo(usuarioId, inicio, fim).stream()
                .map(TransacaoMapper::paraDominio)
                .toList();
    }
}
