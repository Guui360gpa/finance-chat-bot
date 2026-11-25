package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.application.port.in.RegistrarTransacaoComando;
import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.application.port.out.TransacaoRepository;
import br.com.guilherme.financebot.domain.model.Transacao;
import br.com.guilherme.financebot.domain.valueobject.Dinheiro;
import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.domain.valueobject.Usuario;
import br.com.guilherme.financebot.testsupport.NotificadorFalso;
import br.com.guilherme.financebot.testsupport.TransacaoRepositoryEmMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class RegistrarTransacaoServiceTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");
    private static final String MEIO_DIA_EM_SP = "2026-03-01T15:00:00Z"; // 12:00 em São Paulo (UTC-3)

    private final Usuario usuario =
            Usuario.reconstituir(1L, 999L, "Guilherme", SAO_PAULO, Instant.parse("2026-01-01T00:00:00Z"));
    private final TransacaoRepositoryEmMemoria repositorio = new TransacaoRepositoryEmMemoria();
    private final NotificadorFalso notificador = new NotificadorFalso();

    private RegistrarTransacaoService servicoComRelogioEm(String instante) {
        return new RegistrarTransacaoService(repositorio, notificador,
                Clock.fixed(Instant.parse(instante), ZoneOffset.UTC));
    }

    private RegistrarTransacaoComando gasto(LocalDate data) {
        return new RegistrarTransacaoComando(usuario, TipoTransacao.DESPESA, Dinheiro.de("45"),
                "mercado", data, "gastei 45 no mercado");
    }

    @Nested
    @DisplayName("Registro")
    class Registro {

        @Test
        void salvaATransacaoComTodosOsDados() {
            var servico = servicoComRelogioEm(MEIO_DIA_EM_SP);

            Transacao salva = servico.registrar(gasto(LocalDate.of(2026, 3, 1)));

            assertThat(repositorio.todas()).containsExactly(salva);
            assertThat(salva.getId()).isNotNull();
            assertThat(salva.getUsuarioId()).isEqualTo(1L);
            assertThat(salva.getTipo()).isEqualTo(TipoTransacao.DESPESA);
            assertThat(salva.getValor()).isEqualTo(Dinheiro.de("45.00"));
            assertThat(salva.getDescricao()).isEqualTo("mercado");
            assertThat(salva.getMensagemOriginal()).isEqualTo("gastei 45 no mercado");
            assertThat(salva.getCategoriaId()).isNull(); // categorização é a Etapa 2
            assertThat(salva.getCriadaEm()).isEqualTo(Instant.parse(MEIO_DIA_EM_SP));
        }
    }

    @Nested
    @DisplayName("Data e fuso do usuário")
    class DataEFuso {

        @Test
        void dataDeHojeUsaOInstanteAtual() {
            Transacao salva = servicoComRelogioEm(MEIO_DIA_EM_SP).registrar(gasto(LocalDate.of(2026, 3, 1)));

            assertThat(salva.getOcorridaEm()).isEqualTo(Instant.parse(MEIO_DIA_EM_SP));
        }

        @Test
        void dataPassadaUsaMeioDiaNoFusoDoUsuario() {
            Transacao salva = servicoComRelogioEm(MEIO_DIA_EM_SP).registrar(gasto(LocalDate.of(2026, 2, 28)));

            // 28/02 às 12:00 em São Paulo = 15:00 UTC
            assertThat(salva.getOcorridaEm()).isEqualTo(Instant.parse("2026-02-28T15:00:00Z"));
        }

        @Test
        void ofusoDoUsuarioDecideOQueEhHoje() {
            // Em UTC já é 01/03 às 02:00, mas em São Paulo ainda é 28/02 às 23:00.
            String agora = "2026-03-01T02:00:00Z";

            Transacao salva = servicoComRelogioEm(agora).registrar(gasto(LocalDate.of(2026, 2, 28)));

            assertThat(salva.getOcorridaEm()).isEqualTo(Instant.parse(agora));
        }
    }

    @Nested
    @DisplayName("Confirmação ao usuário")
    class Confirmacao {

        @Test
        void confirmaDespesa() {
            servicoComRelogioEm(MEIO_DIA_EM_SP).registrar(gasto(LocalDate.of(2026, 3, 1)));

            assertThat(notificador.enviados()).singleElement().satisfies(envio -> {
                assertThat(envio.destinatario()).isSameAs(usuario);
                assertThat(envio.mensagem()).isEqualTo("Despesa registrada: R$ 45,00 (mercado) em 01/03/2026.");
            });
        }

        @Test
        void confirmaReceitaComDataPassada() {
            var comando = new RegistrarTransacaoComando(usuario, TipoTransacao.RECEITA, Dinheiro.de("3000"),
                    "salário", LocalDate.of(2026, 2, 28), "recebi 3000 salário ontem");

            servicoComRelogioEm(MEIO_DIA_EM_SP).registrar(comando);

            assertThat(notificador.enviados()).singleElement().satisfies(envio ->
                    assertThat(envio.mensagem())
                            .isEqualTo("Receita registrada: R$ 3.000,00 (salário) em 28/02/2026."));
        }

        @Test
        void confirmaMesmoSemDescricao() {
            var comando = new RegistrarTransacaoComando(usuario, TipoTransacao.DESPESA, Dinheiro.de("10"),
                    null, LocalDate.of(2026, 3, 1), "10");

            servicoComRelogioEm(MEIO_DIA_EM_SP).registrar(comando);

            assertThat(notificador.enviados()).singleElement().satisfies(envio ->
                    assertThat(envio.mensagem()).isEqualTo("Despesa registrada: R$ 10,00 em 01/03/2026."));
        }
    }

    @Nested
    @DisplayName("Falhas")
    class Falhas {

        @Test
        void falhaAoNotificarNaoPerdeATransacao() {
            NotificadorPort telegramFora = (destinatario, mensagem) -> {
                throw new IllegalStateException("telegram fora");
            };
            var servico = new RegistrarTransacaoService(repositorio, telegramFora,
                    Clock.fixed(Instant.parse(MEIO_DIA_EM_SP), ZoneOffset.UTC));

            assertThatThrownBy(() -> servico.registrar(gasto(LocalDate.of(2026, 3, 1))))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(repositorio.todas()).hasSize(1);
        }

        @Test
        void falhaAoSalvarNaoNotificaOUsuario() {
            TransacaoRepository bancoFora = new TransacaoRepository() {
                @Override
                public Transacao salvar(Transacao transacao) {
                    throw new IllegalStateException("banco fora");
                }

                @Override
                public List<Transacao> listarNoPeriodo(Long usuarioId, Instant inicio, Instant fim) {
                    throw new IllegalStateException("banco fora");
                }
            };
            var servico = new RegistrarTransacaoService(bancoFora, notificador,
                    Clock.fixed(Instant.parse(MEIO_DIA_EM_SP), ZoneOffset.UTC));

            assertThatThrownBy(() -> servico.registrar(gasto(LocalDate.of(2026, 3, 1))))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(notificador.enviados()).isEmpty();
        }
    }
}