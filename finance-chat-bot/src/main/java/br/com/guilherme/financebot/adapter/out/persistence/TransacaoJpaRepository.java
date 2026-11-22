package br.com.guilherme.financebot.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoJpaRepository extends JpaRepository<TransacaoEntity, Long> {
}
