package br.com.guilherme.financebot.application.port.out;

/** Porta de saída: memória dos updates já tratados (tabela update_processado). */
public interface UpdateProcessadoRepository {


    /**
     * Registra o update de forma atômica.
     *
     * Precisa ser uma única operação (não "consulta e depois grava"): dois envios simultâneos
     * do mesmo update não podem passar juntos pela verificação.
     *
     * @return true se o update é novo (agora registrado); false se já havia sido registrado
     */
    boolean registrarSeNovo(long updateId);
}
