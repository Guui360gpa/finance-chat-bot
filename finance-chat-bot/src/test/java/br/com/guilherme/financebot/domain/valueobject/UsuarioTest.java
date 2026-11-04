package br.com.guilherme.financebot.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UsuarioTest {

    private static final Instant AGORA = Instant.parse("2026-11-04T12:00:00Z");

    @Test
    void novoUsuarioUsaFusoPadraoESemId() {
        Usuario u = Usuario.novo(123456L, "Guilherme", AGORA);

        assertThat(u.getId()).isNull();
        assertThat(u.getTelegramId()).isEqualTo(123456L);
        assertThat(u.getNome()).isEqualTo("Guilherme");
        assertThat(u.getFusoHorario()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
        assertThat(u.getCriadoEm()).isEqualTo(AGORA);
    }

    @Test
    void nomeEhOpcionalENormalizado() {
        assertThat(Usuario.novo(1L, null, AGORA).getNome()).isNull();
        assertThat(Usuario.novo(1L, "   ", AGORA).getNome()).isNull();
        assertThat(Usuario.novo(1L, "  Ana  ", AGORA).getNome()).isEqualTo("Ana");
    }

    @Test
    void rejeitaNomeMuitoLongo() {
        assertThatThrownBy(() -> Usuario.novo(1L, "a".repeat(101), AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstituirPreservaFusoEId() {
        Usuario u = Usuario.reconstituir(5L, 1L, "Ana", ZoneId.of("America/Manaus"), AGORA);

        assertThat(u.getId()).isEqualTo(5L);
        assertThat(u.getFusoHorario()).isEqualTo(ZoneId.of("America/Manaus"));
    }


}
