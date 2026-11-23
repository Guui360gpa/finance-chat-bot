package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.application.port.out.UsuarioRepository;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.util.Optional;

public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Usuario> buscarPorTelegramId(long telegramId) {
        return jpa.findByTelegramId(telegramId).map(UsuarioMapper::paraDominio);
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return UsuarioMapper.paraDominio(jpa.save(UsuarioMapper.paraEntidade(usuario)));
    }
}
