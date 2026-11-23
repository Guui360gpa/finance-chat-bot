package br.com.guilherme.financebot.application.service;


import br.com.guilherme.financebot.application.port.out.UsuarioRepository;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.time.Clock;
import java.util.Objects;

/**
 * Garante que exista um usuário para o telegramId. Colaborador interno da aplicação, por isso
 * sem porta de entrada: nenhum adapter o chama diretamente.
 *
 * Corrida no primeiro contato: se duas mensagens do mesmo usuário novo fossem tratadas ao
 * mesmo tempo, as duas tentariam criar e a UNIQUE de telegram_id recusaria a segunda. O
 * polling é sequencial, então isso não ocorre; e, se ocorresse, o rollback da transação
 * desfaria o update_id e a reentrega encontraria o usuário já criado.
 */
public class ObterOuCriarUsuario {

    private final UsuarioRepository usuarios;
    private final Clock clock;

    public ObterOuCriarUsuario(UsuarioRepository usuarios, Clock clock) {
        this.usuarios = Objects.requireNonNull(usuarios, "usuarios não pode ser nulo");
        this.clock = Objects.requireNonNull(clock, "clock não pode ser nulo");
    }

    public Usuario executar(long telegramId, String nome) {
        return usuarios.buscarPorTelegramId(telegramId)
                .orElseGet(() -> usuarios.salvar(Usuario.novo(telegramId,nome,clock.instant())));
    }
}
