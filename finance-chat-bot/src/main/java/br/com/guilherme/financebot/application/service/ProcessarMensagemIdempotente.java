package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.MensagemRecebida;
import br.com.guilherme.financebot.application.port.in.ProcessarMensagemUseCase;
import br.com.guilherme.financebot.application.port.out.UpdateProcessadoRepository;

import java.util.Objects;


/**
 * Padrão Decorator: embrulha o caso de uso real e acrescenta a idempotência sem que ele saiba.
 * O Telegram pode entregar o mesmo update mais de uma vez (por exemplo, depois de uma queda
 * antes de confirmar o offset). A primeira entrega é processada, as repetidas são ignoradas.
 *
 * Esta classe não captura exceções: se o processamento falhar, o erro sobe. Desfazer o registro
 * do update, para ele poder ser reprocessado, é papel da transação em volta (ver
 * ProcessarMensagemTransacional).
 */
public class ProcessarMensagemIdempotente implements ProcessarMensagemUseCase {

    private static final System.Logger LOG = System.getLogger(ProcessarMensagemIdempotente.class.getName());

    private final UpdateProcessadoRepository updates;
    private final ProcessarMensagemUseCase delegado;

    public ProcessarMensagemIdempotente(UpdateProcessadoRepository updates, ProcessarMensagemUseCase delegado) {
        this.updates = Objects.requireNonNull(updates, "updates não pode ser nulo");
        this.delegado = Objects.requireNonNull(delegado, "delegado não pode ser nulo");
    }

    @Override
    public void processar(MensagemRecebida mensagem) {

        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");

        if (!updates.registrarSeNovo(mensagem.updateId())) {
            LOG.log(System.Logger.Level.INFO, "Update" + mensagem.updateId() + " já processado, ignorando.");
            return;
        }

        delegado.processar(mensagem);

    }
}
