package br.com.guilherme.financebot.domain.valueobject;

import java.util.Objects;

public class Orcamento {

    private final Long id;
    private final Long usuarioId;
    private final Long categoriaId;
    private Dinheiro limite;

    private Orcamento(Long id, Long usuarioId, Long categoriaId, Dinheiro limite) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId,"usuarioId não pode ser nulo");
        this.categoriaId = Objects.requireNonNull(categoriaId,"categoriaId não pode ser nulo");
        this.limite = validarLimite(limite);
    }

    private Dinheiro validarLimite(Dinheiro limite) {
        Objects.requireNonNull(limite,"limite não pode ser nulo");

        if (!limite.ehPositivo()) {
            throw new IllegalArgumentException("limite deve ser maior que zero");
        }
        return limite;
    }


}
