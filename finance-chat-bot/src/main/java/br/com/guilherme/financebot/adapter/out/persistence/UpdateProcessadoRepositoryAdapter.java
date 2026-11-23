package br.com.guilherme.financebot.adapter.out.persistence;

import br.com.guilherme.financebot.application.port.out.UpdateProcessadoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UpdateProcessadoRepositoryAdapter implements UpdateProcessadoRepository {

    private static final String INSERIR_SE_NOVO =
            "INSERT INTO update_processado (update_id) VALUES (?) ON CONFLICT (update_id) DO NOTHING";

    private final JdbcTemplate jdbc;

    public UpdateProcessadoRepositoryAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean registrarSeNovo(long updateId) {
        return jdbc.update(INSERIR_SE_NOVO,updateId) == 1;
    }
}
