package pe.ferreteria.dao;

import org.junit.jupiter.api.Test;
import pe.ferreteria.model.Producto;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ProductoDAOMemoriaTest {
    private final ProductoDAO dao = new ProductoDAOMemoria();

    @Test
    void registraUnProductoYLoIncluyeEnLaConsulta() {
        dao.registrar(producto("NUEVO-1", "12.50"));
        Producto guardado = dao.listar().stream().filter(p -> p.getSku().equals("NUEVO-1")).findFirst().orElseThrow();
        assertEquals("Producto de prueba", guardado.getNombre());
        assertEquals(new BigDecimal("12.50"), guardado.getPrecio());
    }

    @Test
    void rechazaSkuDuplicadoAunqueCambieMayusculasOEspacios() {
        int anteriores = dao.listar().size();
        assertThrows(IllegalArgumentException.class, () -> dao.registrar(producto(" fr-1042 ", "1.00")));
        assertEquals(anteriores, dao.listar().size());
        assertEquals(new BigDecimal("28.50"), dao.listar().get(0).getPrecio());
    }

    @Test
    void noPermiteModificarElListadoDesdeFuera() {
        assertThrows(UnsupportedOperationException.class, () -> dao.listar().clear());
    }

    @Test
    void validaPrecioYCategoriaYCamposObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> producto("N-1", "-1"));
        assertThrows(IllegalArgumentException.class, () -> producto("N-1", "1.001"));
        assertThrows(IllegalArgumentException.class, () -> producto("", "1"));
        assertThrows(IllegalArgumentException.class, () -> new Producto("N-1", " ", "Ferretería", "Unidad", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Producto("N-1", "Prueba", "Otra", "Unidad", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> new Producto("N-1", "Prueba", "Ferretería", " ", BigDecimal.ONE));
    }

    private Producto producto(String sku, String precio) {
        return new Producto(sku, "Producto de prueba", "Ferretería", "Unidad", new BigDecimal(precio));
    }
}
