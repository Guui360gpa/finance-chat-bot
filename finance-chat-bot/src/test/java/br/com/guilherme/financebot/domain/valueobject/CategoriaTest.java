package br.com.guilherme.financebot.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CategoriaTest {

    @Test
    void criaCategoriaComNomeNormalizado(){
        Categoria c = Categoria.nova(1L," mercado    ");
        assertThat(c.getId()).isNull();
        assertThat(c.getUsuarioId()).isEqualTo(1L);
        assertThat(c.getNome()).isEqualTo("mercado");
    }

    @Test
    void rejeitaNomeVazioOuNulo() {
        assertThatThrownBy(() -> Categoria.nova(1L,""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Categoria.nova(1L,null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejeitaNomeMuitoLongo() {
        assertThatThrownBy(() -> Categoria.nova(1L,"a".repeat(51)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaUsuarioNulo() {
        assertThatThrownBy(() -> Categoria.nova(null,"mercado"))
                .isInstanceOf(NullPointerException.class);
    }
}
