package pe.ferreteria.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMovimientoTest {
    @Test
    void normalizaElSkuAMayusculas() {
        assertEquals("FR-1042", new ItemMovimiento(" fr-1042 ", 2).getSku());
    }

    @Test
    void rechazaCantidadCeroONegativa() {
        assertThrows(IllegalArgumentException.class, () -> new ItemMovimiento("FR-1042", 0));
        assertThrows(IllegalArgumentException.class, () -> new ItemMovimiento("FR-1042", -3));
    }

    @Test
    void rechazaSkuConCaracteresPeligrosos() {
        assertThrows(IllegalArgumentException.class, () -> new ItemMovimiento("A'; DROP TABLE producto;--", 1));
    }

    @Test
    void convierteParametrosParalelosIgnorandoFilasVacias() {
        List<ItemMovimiento> items = ItemMovimiento.desdeParametros(
                new String[]{"CN-0087", "", "FR-1042"}, new String[]{"5", "", "2"});
        assertEquals(2, items.size());
        assertEquals(5, items.get(0).getCantidad());
    }

    @Test
    void rechazaCantidadNoNumerica() {
        assertThrows(IllegalArgumentException.class, () -> ItemMovimiento.desdeParametros(
                new String[]{"CN-0087"}, new String[]{"abc"}));
    }

    @Test
    void rechazaFormularioSinLineas() {
        assertThrows(IllegalArgumentException.class, () -> ItemMovimiento.desdeParametros(
                new String[]{""}, new String[]{""}));
        assertThrows(IllegalArgumentException.class, () -> ItemMovimiento.desdeParametros(null, null));
    }
}
