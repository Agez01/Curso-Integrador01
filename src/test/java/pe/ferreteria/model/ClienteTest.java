package pe.ferreteria.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {
    @Test
    void aceptaDniYRucYIdentificaElTipo() {
        assertEquals("DNI", new Cliente("45871236", "Carlos Quispe", "", "").getTipoDocumento());
        assertEquals("RUC", new Cliente("20512345678", "Constructora Andina", "014445566", "Lima").getTipoDocumento());
    }

    @Test
    void rechazaDocumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente("1234", "Ana", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Cliente("12345678a", "Ana", "", ""));
    }

    @Test
    void rechazaNombreVacioOTelefonoConLetras() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente("45871236", "  ", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Cliente("45871236", "Ana", "abc", ""));
    }
}
