package br.com.guilherme.financebot.testsupport;

import br.com.guilherme.financebot.application.port.out.UsuarioRepository;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class UsuarioRepositoryEmMemoria implements UsuarioRepository {

    private final Map<Long, Usuario> porTelegramId = new LinkedHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private int salvamentos;


    @Override
    public Optional<Usuario> buscarPorTelegramId(long telegramId) {
        return Optional.ofNullable(porTelegramId.get(telegramId));
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        salvamentos++;

        Usuario salvo = Usuario.reconstituir(sequencia.incrementAndGet(), usuario.getTelegramId(), usuario.getNome(),
                usuario.getFusoHorario(), usuario.getCriadoEm());
        porTelegramId.put(salvo.getTelegramId(),salvo);

        return salvo;
    }

    public int total() {
        return porTelegramId.size();
    }

    public int salvamentos() {
        return salvamentos;
    }
}
