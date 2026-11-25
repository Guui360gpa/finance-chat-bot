package br.com.guilherme.financebot.config;

import br.com.guilherme.financebot.application.parser.ComandoParser;
import br.com.guilherme.financebot.application.parser.GastoRegexParser;
import br.com.guilherme.financebot.application.parser.InterpretadorMensagem;
import br.com.guilherme.financebot.application.port.in.ConsultarExtratoDoDiaUseCase;
import br.com.guilherme.financebot.application.port.in.ProcessarMensagemUseCase;
import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoUseCase;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.application.port.out.UpdateProcessadoRepository;
import br.com.guilherme.financebot.application.port.out.UsuarioRepository;
import br.com.guilherme.financebot.application.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;


/**
 * Montagem da aplicação: liga as classes Java puras às portas implementadas pelos adapters.
 * O ProcessarMensagemUseCase é o ÚNICO bean desse tipo; o serviço real fica escondido por dentro:
 *
 *   Transacional( Idempotente( ProcessarMensagemService ) )
 *
 * A transação fica por fora para englobar o registro do update_id e o tratamento da mensagem.
 */
@Configuration
public class AplicacaoConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    RegistrarTransacaoUseCase registrarTransacaoUseCase(TransacaoRepository transacoes,
                                                        NotificadorPort notificador, Clock clock) {
        return new RegistrarTransacaoService(transacoes, notificador, clock);
    }

    @Bean
    ConsultarExtratoDoDiaUseCase consultarExtratoDoDiaUseCase(TransacaoRepository transacoes) {
        return new ConsultarExtratoDoDiaService(transacoes);
    }

    @Bean
    ProcessarMensagemUseCase processarMensagemUseCase(UsuarioRepository usuarios,
                                                      RegistrarTransacaoUseCase registrarTransacao,
                                                      ConsultarExtratoDoDiaUseCase consultarExtratoDoDia,
                                                      NotificadorPort notificador,
                                                      UpdateProcessadoRepository updates,
                                                      Clock clock) {
        var interpretador = new InterpretadorMensagem(List.of(new ComandoParser(), new GastoRegexParser()));
        var servico = new ProcessarMensagemService(
                new ObterOuCriarUsuario(usuarios, clock), interpretador, registrarTransacao,
                consultarExtratoDoDia, notificador, clock);
        return new ProcessarMensagemTransacional(new ProcessarMensagemIdempotente(updates, servico));
    }


}
