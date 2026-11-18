package br.com.guilherme.financebot.application.port.out;

import br.com.guilherme.financebot.domain.valueobject.Transacao;

/**
 * Porta de saída: o que a aplicação precisa para guardar transações.
 * A interface pertence à camada application; quem a implementa (JPA) fica em adapter/out/persistence.
 * Só tem o que a E1 usa. Consultas (/hoje, /resumo) entram quando forem necessárias.
 */

public interface TransacaoRepository {

    //Persiste a transação e devolve a versão salva, já com id.
    Transacao salvar(Transacao transacao);
}
