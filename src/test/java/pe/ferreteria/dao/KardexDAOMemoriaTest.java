package pe.ferreteria.dao;

import org.junit.jupiter.api.Test;
import pe.ferreteria.model.MovimientoKardex;

import static org.junit.jupiter.api.Assertions.*;

class KardexDAOMemoriaTest {
    private final KardexDAO dao = new KardexDAOMemoria();

    @Test
    void unaEntradaSumaAlStock() {
        MovimientoKardex m = dao.registrar("CN-0087", "Cemento Sol Tipo I", "Entrada", 10, "admin");
        assertEquals(93, m.getStockResultante());
    }

    @Test
    void unaSalidaRestaDelStock() {
        MovimientoKardex m = dao.registrar("CN-0087", "Cemento Sol Tipo I", "Salida", 3, "cajero");
        assertEquals(80, m.getStockResultante());
    }

    @Test
    void unaSalidaMayorAlStockSeRechaza() {
        assertThrows(IllegalStateException.class,
                () -> dao.registrar("CN-0087", "Cemento Sol Tipo I", "Salida", 1000, "cajero"));
    }

    @Test
    void cantidadCeroOTipoInvalidoSeRechazan() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.registrar("x", "Cemento Sol Tipo I", "Entrada", 0, "admin"));
        assertThrows(IllegalArgumentException.class,
                () -> dao.registrar("x", "Cemento Sol Tipo I", "Merma", 1, "admin"));
    }

    @Test
    void elMovimientoNuevoAparecePrimeroEnElListado() {
        dao.registrar("TV-1", "Producto nuevo", "Entrada", 5, "admin");
        assertEquals("Producto nuevo", dao.listar().get(0).getProducto());
    }
}
