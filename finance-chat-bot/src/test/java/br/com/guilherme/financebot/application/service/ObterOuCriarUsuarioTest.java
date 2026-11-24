package br.com.guilherme.financebot.application.service;

import br.com.guilherme.financebot.domain.valueobject.Usuario;
import br.com.guilherme.financebot.testsupport.UsuarioRepositoryEmMemoria;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ObterOuCriarUsuarioTest {

    private static final Instant AGORA = Instant.parse("2026-03-01T15:00:00Z");

    private final UsuarioRepositoryEmMemoria usuarios = new UsuarioRepositoryEmMemoria();
    private final ObterOuCriarUsuario caso = new ObterOuCriarUsuario(usuarios, Clock.fixed(AGORA, ZoneOffset.UTC));

    @Test
    void criaOUsuarioNoPrimeiroContato() {
        Usuario usuario = caso.executar(999L, "Guilherme");

        assertThat(usuario.getId()).isNotNull();
        assertThat(usuario.getTelegramId()).isEqualTo(999L);
        assertThat(usuario.getNome()).isEqualTo("Guilherme");
        assertThat(usuario.getFusoHorario()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
        assertThat(usuario.getCriadoEm()).isEqualTo(AGORA);
        assertThat(usuarios.total()).isEqualTo(1);
    }

    @Test
    void reaproveitaOUsuarioNosContatosSeguintes() {
        Usuario primeiro = caso.executar(999L, "Guilherme");
        Usuario segundo = caso.executar(999L, "Outro Nome");

        assertThat(segundo.getId()).isEqualTo(primeiro.getId());
        assertThat(segundo.getNome()).isEqualTo("Guilherme");
        assertThat(usuarios.total()).isEqualTo(1);
        assertThat(usuarios.salvamentos()).isEqualTo(1);
    }

    @Test
    void pessoasDiferentesViramUsuariosDiferentes() {
        Usuario a = caso.executar(999L, "Ana");
        Usuario b = caso.executar(1000L, "Bia");

        assertThat(a.getId()).isNotEqualTo(b.getId());
        assertThat(usuarios.total()).isEqualTo(2);
    }

    @Test
    void nomeEhOpcional() {
        assertThat(caso.executar(999L, null).getNome()).isNull();
    }

}