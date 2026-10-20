package pe.ferreteria.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test
    void laClaveCorrectaSeVerifica() {
        String guardado = PasswordUtil.hashear("secreta123");
        assertTrue(PasswordUtil.verificar("secreta123", guardado));
    }

    @Test
    void unaClaveIncorrectaSeRechaza() {
        String guardado = PasswordUtil.hashear("secreta123");
        assertFalse(PasswordUtil.verificar("otra", guardado));
    }

    @Test
    void dosHashDeLaMismaClaveSonDistintosPorLaSal() {
        assertNotEquals(PasswordUtil.hashear("abc"), PasswordUtil.hashear("abc"));
    }

    @Test
    void unFormatoInvalidoNoLanzaExcepcion() {
        assertFalse(PasswordUtil.verificar("abc", "texto-sin-formato"));
        assertFalse(PasswordUtil.verificar(null, "1:2:3"));
    }
}
