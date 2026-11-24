package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.domain.valueobject.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private static final Instant AGORA = Instant.parse("2026-03-01T15:00:00Z");

    @Test
    void usuarioNovoViraEntidadeSemIdEComFusoEmTexto() {
        Usuario novo = Usuario.novo(999L, "Guilherme", AGORA);

        UsuarioEntity entidade = UsuarioMapper.paraEntidade(novo);

        assertThat(entidade.getId()).isNull();
        assertThat(entidade.getTelegramId()).isEqualTo(999L);
        assertThat(entidade.getNome()).isEqualTo("Guilherme");
        assertThat(entidade.getFusoHorario()).isEqualTo("America/Sao_Paulo");
        assertThat(entidade.getCriadoEm()).isEqualTo(AGORA);
    }

    @Test
    void idaEVoltaPreservaTodosOsCampos() {
        Usuario original = Usuario.reconstituir(5L, 999L, "Ana", ZoneId.of("America/Manaus"), AGORA);

        Usuario volta = UsuarioMapper.paraDominio(UsuarioMapper.paraEntidade(original));

        assertThat(volta).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    void nomeNuloEhPreservado() {
        UsuarioEntity entidade = new UsuarioEntity(5L, 999L, null, "America/Sao_Paulo", AGORA);

        assertThat(UsuarioMapper.paraDominio(entidade).getNome()).isNull();
    }

}