package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.ZoneId;
import java.util.Objects;

/** Converte entre o modelo de domínio e a entidade JPA. O fuso vira texto na ida e ZoneId na volta. */
public class UsuarioMapper {

    private UsuarioMapper() {
    }

    static UsuarioEntity paraEntidade(Usuario u) {
        Objects.requireNonNull(u, "usuario não pode ser nulo");
        return new UsuarioEntity(u.getId(), u.getTelegramId(), u.getNome(), u.getFusoHorario().getId(), u.getCriadoEm());
    }

    static Usuario paraDominio(UsuarioEntity e) {
        Objects.requireNonNull(e, "entidade não pode ser nula");
        return Usuario.reconstituir(e.getId(), e.getTelegramId(), e.getNome(),
                ZoneId.of(e.getFusoHorario()), e.getCriadoEm());
    }
}
