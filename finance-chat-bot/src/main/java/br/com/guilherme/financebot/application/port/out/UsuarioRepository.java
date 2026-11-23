package br.com.guilherme.financebot.application.port.out;

import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.util.Optional;

/** Porta de saída: guarda e consulta usuários. */
public interface UsuarioRepository {

    Optional<Usuario> buscarPorTelegramId(long telegramId);

    /** Persiste o usuário e devolve a versão salva, já com id. */
    Usuario salvar(Usuario usuario);
}
