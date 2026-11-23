package br.com.guilherme.financebot.config;

import br.com.guilherme.financebot.application.port.in.MensagemRecebida;
import br.com.guilherme.financebot.application.port.in.ProcessarMensagemUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


/**
 * Fronteira de transação do processamento de uma mensagem. Fica na camada mais externa da
 * composição, de modo que "registrar o update_id" e "tratar a mensagem" confirmem (commit) ou
 * desfaçam (rollback) juntos: se o tratamento falhar, o update_id não fica gravado e pode ser
 * reprocessado na próxima entrega.
 *
 * Mantém a aplicação livre de anotações do Spring: só esta classe de config conhece @Transactional.
 */
@Transactional
public class ProcessarMensagemTransacional implements ProcessarMensagemUseCase {

    private final ProcessarMensagemUseCase delegado;

    public ProcessarMensagemTransacional(ProcessarMensagemUseCase delegado) {
        this.delegado = Objects.requireNonNull(delegado, "delegado não pode ser nulo");
    }

    @Override
    public void processar(MensagemRecebida mensagem) {
        delegado.processar(mensagem);
    }
}
