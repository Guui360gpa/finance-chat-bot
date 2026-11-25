package br.com.guilherme.financebot.application.port.out;







import br.com.guilherme.financebot.domain.model.Transacao;

import java.time.Instant;
import java.util.List;

/**
 * Porta de saída: o que a aplicação precisa para guardar transações.
 * A interface pertence à camada application; quem a implementa (JPA) fica em adapter/out/persistence.
 * Só tem o que a E1 usa. Consultas (/hoje, /resumo) entram quando forem necessárias.
 */
public interface TransacaoRepository {

    //Persiste a transação e devolve a versão salva, já com id.
    Transacao salvar(Transacao transacao);

    /**
     * Transações do usuário com ocorridaEm no intervalo semiaberto [inicio, fim), da mais antiga
     * para a mais recente. Inclui as desfeitas: ignorá-las é regra do domínio (Extrato).
     */
    List<Transacao> listarNoPeriodo(Long usuarioId, Instant inicio, Instant fim);
}
