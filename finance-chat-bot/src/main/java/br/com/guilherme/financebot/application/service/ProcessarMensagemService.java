package br.com.guilherme.financebot.application.service;


import br.com.guilherme.financebot.application.parser.*;
import br.com.guilherme.financebot.application.port.in.*;
import br.com.guilherme.financebot.application.port.out.NotificacaoFalhouException;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Fluxo de entrada: resolve o usuário, interpreta o texto e despacha para o caso de uso certo.
 * Como MensagemInterpretada é selada, o switch abaixo é exaustivo: se um novo tipo de resultado
 * for criado, o código deixa de compilar até ele ser tratado aqui.
 *
 * Falha ao notificar nunca desfaz o que já foi feito: é registrada em log e o processamento
 * segue. (Quem chama dentro de @Transactional não vê a exceção, então não há rollback.)
 */
public class ProcessarMensagemService implements ProcessarMensagemUseCase {

    private static final System.Logger LOG = System.getLogger(ProcessarMensagemService.class.getName());

    private final ObterOuCriarUsuario obterOuCriarUsuario;
    private final InterpretadorMensagem interpretador;
    private final RegistrarTransacaoUseCase registrarTransacao;
    private final ConsultarExtratoDoDiaUseCase consultarExtratoDoDia;
    private final NotificadorPort notificador;
    private final Clock clock;

    public ProcessarMensagemService(ObterOuCriarUsuario obterOuCriarUsuario,
                                    InterpretadorMensagem interpretador,
                                    RegistrarTransacaoUseCase registrarTransacao,
                                    ConsultarExtratoDoDiaUseCase consultarExtratoDoDia,
                                    NotificadorPort notificador,
                                    Clock clock) {
        this.obterOuCriarUsuario = Objects.requireNonNull(obterOuCriarUsuario);
        this.interpretador = Objects.requireNonNull(interpretador);
        this.registrarTransacao = Objects.requireNonNull(registrarTransacao);
        this.consultarExtratoDoDia = Objects.requireNonNull(consultarExtratoDoDia);
        this.notificador = Objects.requireNonNull(notificador);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void processar(MensagemRecebida mensagem) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");

        Usuario usuario = obterOuCriarUsuario.executar(mensagem.telegramId(), mensagem.nome());
        LocalDate hoje = LocalDate.ofInstant(clock.instant(), usuario.getFusoHorario());
        MensagemInterpretada interpretada = interpretador.interpretar(mensagem.texto(), hoje);

        try {
            switch (interpretada) {
                case GastoInterpretado gasto -> tratarGasto(usuario, mensagem, gasto);
                case MensagemIncompleta incompleta ->
                        notificador.notificar(usuario, Respostas.valorSemDescricao(incompleta.valor()));
                case ComandoInterpretado comando -> tratarComando(usuario, comando, hoje);
                case NaoReconhecida naoReconhecida -> notificador.notificar(usuario, Respostas.AJUDA);
            }
        } catch (NotificacaoFalhouException e) {
            LOG.log(System.Logger.Level.WARNING, "Não foi possível notificar o usuário " + usuario.getId() + ": " + e.getMessage());
        }
    }

    private void tratarGasto(Usuario usuario, MensagemRecebida mensagem, GastoInterpretado gasto) {
        if (gasto.valor().ehMaiorQue(Transacao.VALOR_MAXIMO)) {
            notificador.notificar(usuario, Respostas.valorAcimaDoLimite());
            return;
        }
        registrarTransacao.registrar(new RegistrarTransacaoComando(usuario, gasto.tipo(), gasto.valor(),
                gasto.descricao(), gasto.data(), mensagem.texto()));
    }

    private void tratarComando(Usuario usuario, ComandoInterpretado interpretado, LocalDate hoje) {
        switch (interpretado.comando()) {
            case AJUDA -> notificador.notificar(usuario, Respostas.AJUDA);
            case HOJE -> notificador.notificar(usuario,
                    Respostas.extratoDoDia(hoje, consultarExtratoDoDia.consultar(usuario, hoje)));
            // Os demais comandos chegam nas Etapas 2 a 4.
            default -> notificador.notificar(usuario, Respostas.COMANDO_INDISPONIVEL);
        }
    }
}
