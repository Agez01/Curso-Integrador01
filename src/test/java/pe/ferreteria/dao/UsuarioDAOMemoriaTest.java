package pe.ferreteria.dao;

import org.junit.jupiter.api.Test;
import pe.ferreteria.model.Rol;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOMemoriaTest {
    private final UsuarioDAO dao = new UsuarioDAOMemoria();

    @Test
    void autenticaConClaveCorrectaYDevuelveSuRol() {
        assertEquals(Rol.CAJERO, dao.autenticar("cajero", "cajero123").orElseThrow().getRol());
    }

    @Test
    void rechazaClaveIncorrecta() {
        assertTrue(dao.autenticar("admin", "mala").isEmpty());
    }

    @Test
    void rechazaUsuarioInexistenteONulo() {
        assertTrue(dao.autenticar("noexiste", "x").isEmpty());
        assertTrue(dao.autenticar(null, null).isEmpty());
    }
}
