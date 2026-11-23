package br.com.guilherme.financebot.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Detalhe do Spring Data (package-private). A consulta é derivada do nome do método. */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByTelegramId(Long telegramId);
}
