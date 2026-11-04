package br.com.guilherme.financebot.domain.valueobject;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public final class Transacao {

    private static final int DESCRICAO_MAX = 200;

    private final Long id; // null até ser persistida
    private final Long usuarioId;
    private Long categoriaId; // null = "Sem categoria"
    private final TipoTransacao tipo;
    private final Dinheiro valor;
    private final String descricao;
    private final Instant ocorridaEm;
    private final String mensagemOriginal;
    private Instant desfeitaEm;
    private final Instant criadaEm;

    public Transacao(Long id, Long usuarioId, Long categoriaId, TipoTransacao tipo, Dinheiro valor, String descricao, Instant ocorridaEm, String mensagemOriginal, Instant desfeitaEm, Instant criadaEm) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId,"id não pode ser nulo");
        this.categoriaId = categoriaId;
        this.tipo = Objects.requireNonNull(tipo,"tipo não pode ser nulo");
        this.valor = validarValor(valor);
        this.descricao = normalizarDescricao(descricao);
        this.ocorridaEm = Objects.requireNonNull(ocorridaEm, "ocorridaEm não pode ser nulo");
        this.mensagemOriginal = mensagemOriginal;
        this.desfeitaEm = desfeitaEm;
        this.criadaEm = Objects.requireNonNull(criadaEm, "criadaEm não pode ser nulo");
    }

    public static Transacao registrar(Long usuarioId, TipoTransacao tipo, Dinheiro valor, String descricao, Instant ocorridaEm, String mensagemOriginal, Instant agora) {

        return new Transacao(null, usuarioId, null, tipo, valor, descricao, ocorridaEm,
                mensagemOriginal, null, agora);

    }

    public static Transacao reconstituir(Long id, Long usuarioId, Long categoriaId, TipoTransacao tipo, Dinheiro valor, String descricao, Instant ocorridaEm, String mensagemOriginal, Instant desfeitaEm, Instant criadaEm) {

        return new Transacao(Objects.requireNonNull(id, "id não pode ser nulo"), usuarioId, categoriaId,
                tipo, valor, descricao, ocorridaEm, mensagemOriginal, desfeitaEm, criadaEm);

    }

    public void categorizar(Long categoriaId) {
        Objects.requireNonNull(categoriaId, "categoriaId não pode ser nulo");
        if (ehDesfeita()) {
            throw new IllegalStateException("transação desfeita não pode ser recategorizada");
        }
        this.categoriaId = categoriaId;
    }

    public void desfazer(Instant agora) {
        Objects.requireNonNull(agora, "agora não pode ser nulo");
        if (ehDesfeita()) {
            throw new IllegalStateException("transação já foi desfeita");
        }
        this.desfeitaEm = agora;
    }

    public boolean ehDesfeita() { return desfeitaEm != null; }
    public boolean ehDespesa() { return tipo == TipoTransacao.DESPESA; }
    public boolean ehReceita() { return tipo == TipoTransacao.RECEITA; }

    private static Dinheiro validarValor(Dinheiro valor) {
        Objects.requireNonNull(valor, "valor não pode ser nulo");
        if (!valor.ehPositivo()) {
            throw new IllegalArgumentException("valor da transação deve ser maior que zero");
        }

        return valor;
    }

    private static String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        String limpa = descricao.strip();

        return limpa.length() > DESCRICAO_MAX ? limpa.substring(0,DESCRICAO_MAX) : limpa;
    }
}
