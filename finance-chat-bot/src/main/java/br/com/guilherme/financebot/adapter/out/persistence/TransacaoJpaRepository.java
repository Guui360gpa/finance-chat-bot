package br.com.guilherme.financebot.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/** Detalhe do Spring Data: package-private para nada fora deste pacote depender dele. */
public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, Long> {

    /** Intervalo semiaberto [inicio, fim). Usa o índice (usuario_id, ocorrida_em) da migration V1. */
    @Query("""
            select t from TransacaoEntity t
            where t.usuarioId = :usuarioId
              and t.ocorridaEm >= :inicio
              and t.ocorridaEm < :fim
            order by t.ocorridaEm""")
    List<TransacaoEntity> buscarNoPeriodo(@Param("usuarioId") Long usuarioId,
                                          @Param("inicio") Instant inicio,
                                          @Param("fim") Instant fim);
}
