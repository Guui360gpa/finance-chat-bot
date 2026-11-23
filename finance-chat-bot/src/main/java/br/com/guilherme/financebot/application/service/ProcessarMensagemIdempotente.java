package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.MensagemRecebida;
import br.com.guilherme.financebot.application.port.in.ProcessarMensagemUseCase;



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



    @Override
    public void processar(MensagemRecebida mensagem) {

    }
}
