package br.com.guilherme.financebot.testsupport;

import br.com.guilherme.financebot.application.port.out.NotificadorPort;
import br.com.guilherme.financebot.domain.valueobject.Usuario;

import java.util.ArrayList;
import java.util.List;

public class NotificadorFalso implements NotificadorPort {

    public record Envio(Usuario destinatario, String mensagem){}

    private final List<Envio> enviados = new ArrayList<>();


    @Override
    public void notificar(Usuario destinatario, String mensagem) {
        enviados.add(new Envio(destinatario,mensagem));
    }

    public List<Envio> enviados() {
        return List.copyOf(enviados);
    }
}
