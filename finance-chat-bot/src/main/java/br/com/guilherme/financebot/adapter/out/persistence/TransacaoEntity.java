package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.domain.valueobject.TipoTransacao;
import br.com.guilherme.financebot.domain.valueobject.Transacao;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representação da tabela "transacao" (migration V1). Detalhe de persistência: o resto da
 * aplicação só enxerga o modelo de domínio, convertido pelo TransacaoMapper.
 *
 * Referências a outras tabelas são colunas simples (usuario_id, categoria_id), não @ManyToOne:
 * o domínio também referencia por id, e a integridade já é garantida pelas FKs do banco.
 */


@Entity
@Table(name = "transacao")
@Getter
public class TransacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id",nullable = false)
    private Long usuarioId;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private TipoTransacao tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 200)
    private String descricao;

    @Column(name = "ocorrida_em", nullable = false)
    private Instant ocorridaEm;

    @Column(name = "mensagem_original", columnDefinition = "TEXT")
    private String mensagemOriginal;

    @Column(name = "desfeita_em")
    private Instant desfeitaEm;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    public TransacaoEntity() {}

    public TransacaoEntity(Long id, Long usuarioId, Long categoriaId, TipoTransacao tipo, BigDecimal valor, String descricao,
                           Instant ocorridaEm, String mensagemOriginal, Instant criadaEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.categoriaId = categoriaId;
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
        this.ocorridaEm = ocorridaEm;
        this.mensagemOriginal = mensagemOriginal;
        this.criadaEm = criadaEm;
    }

    public TransacaoEntity(Transacao t){
                this.id = t.getId();
                this.usuarioId = t.getUsuarioId();
                this.categoriaId = t.getCategoriaId();
                this.tipo = t.getTipo();
                this.valor = t.getValor().valor();
                this.descricao = t.getDescricao();
                this.ocorridaEm = t.getOcorridaEm();
                this.mensagemOriginal = t.getMensagemOriginal();
                this.desfeitaEm = t.getDesfeitaEm();
                this.criadaEm = t.getCriadaEm();
    }
}
