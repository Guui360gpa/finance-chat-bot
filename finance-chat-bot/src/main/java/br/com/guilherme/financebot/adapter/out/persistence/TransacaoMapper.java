package br.com.guilherme.financebot.adapter.out.persistence;



import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;

import java.time.Instant;
import java.util.Objects;

/** Converte entre o modelo de domínio e a entidade JPA. Sem Spring, fácil de testar. */
public class TransacaoMapper {

    private TransacaoMapper() {}

    static TransacaoEntity paraEntidade(Transacao t) {
        Objects.requireNonNull(t , "transacao não pode ser nula");

        return new TransacaoEntity(t);
    }

    static Transacao paraDominio(TransacaoEntity e) {
        Objects.requireNonNull(e, "entidade não pode ser nula");

        return Transacao.reconstituir(e.getId(), e.getUsuarioId(), e.getCategoriaId(), e.getTipo(),
                Dinheiro.de(e.getValor()), e.getDescricao(), e.getOcorridaEm(),
                e.getMensagemOriginal(), e.getDesfeitaEm(), e.getCriadaEm());
    }
}
